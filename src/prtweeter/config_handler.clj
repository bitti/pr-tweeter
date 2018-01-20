(ns prtweeter.config-handler
  (:require [clojure.java.io :as io :refer [file]]
            [clojure.spec.alpha :as s]
            [environ.core :refer [env]]
            [prtweeter.github-client :refer [get-repository-info]]
            [prtweeter.helper :refer :all]
            [prtweeter.twitter-client :as twitter]
            [rewrite-clj.zip :as z]))

(def ^:const default-template
  "{{name|abbreviate:15:…}} PR by {{login|abbreviate:12:…}} on {{created_at|date:\"M/d/yy HH:mm z\":us}}: {{title|abbreviate:60:…}} {{html_url}}")
(def ^:const default-limit 5)
(def ^:const xdg-config-home (join-once \/ (env :home) ".config"))

(def user-configuration-file (file xdg-config-home "pr-tweeter.edn"))
(def local-configuration-file (file ".pr-tweeter.edn"))

;;; Config format specification
(s/def ::config-spec
  (s/keys :req-un [::earliest-pr ::pr-limit-per-run ::github ::twitter]))
(s/def ::earliest-pr inst?)
(s/def ::pr-limit-per-run (s/and integer? pos?))
(s/def ::github
  (s/keys :req-un [::user ::repository]))
(s/def ::user string?)
(s/def ::repository string?)
(s/def ::twitter
  (s/keys :req-un [::consumer-key ::consumer-secret ::access-token ::access-token-secret]))
(s/def ::consumer-key string?)
(s/def ::consumer-secret string?)
(s/def ::access-token string?)
(s/def ::access-token-secret string?)

(defn- read-config [config-file]
  (println "Reading configuration from" (str config-file))
  (-> config-file
      io/reader
      java.io.PushbackReader.
      clojure.edn/read
      ((fn [config]
         (if-not (s/valid? ::config-spec config)
           (abort (str "Problem while reading config file:\n"
                       (s/explain-str ::config-spec config)))
           (with-meta config {:file config-file}))))
      ))

(defn- interactive-github-configuration []
  (let [github-user (prompt "Github user name: ")
        github-repository (prompt "Github repository name: ")]
    (println "Testing github connection...")
    (abort-on-error
     #(str "\nEncountered a problem when contacting github:\n\n"
            (:body (.data %))
            "\n\nPlease check the provided names and your network connection.")
     (get-repository-info github-user github-repository))
    (println "OK")
    { :user github-user :repository github-repository }
    ))

(defn- interactive-twitter-app-configuration []
  (-> "In order to tweet on your behalf, this App needs valid keys and
  access tokens. If you don't have these available please log into
  your twitter account, go to https://apps.twitter.com and chose
  'Create New App'. This App needs at least 'Read and Write' access."
      word-wrap println)
  (let [consumer-key (prompt "Consumer Key: ")
        consumer-secret (prompt "Consumer Secret: ")]
    (-> "This App doesn't support automatic OAuth token acquisition
    (yet). Please use the 'Keys and Access Tokens' tab in the twitter
    application settings and use the 'Create my access token' button
    to create an 'Access Token' and 'Access Tocken Secret'"
        word-wrap println)
    (let [access-token (prompt "Access Token: ")
          access-token-secret (prompt "Access Token Secret: ")
          credentials {:consumer-key consumer-key
                       :consumer-secret consumer-secret
                       :access-token access-token
                       :access-token-secret access-token-secret
                       }
          ]
      (println "Testing twitter connection...")
      (abort-on-error twitter/default-error (twitter/user-info credentials))
      (println "OK")
      credentials)))

(defn- interactive-start-date-configuration []
  "https://github.com/rails/rails/pulls?q=is%3Aopen"
  )

(defn- create-new-config-interactively []
  (println "No configuration found. A new configuration will be created at"
           (str user-configuration-file))
  (let [config {:github (interactive-github-configuration)
                :twitter (interactive-twitter-app-configuration)
                :earliest-pr (java.util.Date.)
                :pr-limit-per-run default-limit
                :status-template default-template
                }]
    (clojure.pprint/pprint config (clojure.java.io/writer user-configuration-file))
    (println "Configuration written to" (str user-configuration-file))
    (-> "Please remember to keep this file at a safe location as it
    contains credentials to access your twitter account!"
        word-wrap println)
    (prompt "Press enter to start the first query of"
            (get-in config [:github :repository]) "for new PRs")
    (with-meta config {:file user-configuration-file})
    ))

(defn update-earliest-pr!
  "Update the :earliest-pr attribute for the given config to given
  date while preserving whitespace and comments."
  [config date]
  (let [configuration-file (:file (meta config))
        zipper
        (-> configuration-file
            slurp
            z/of-string
            z/down
            (z/find-value :earliest-pr)
            z/next
            (z/replace date)
            )]
    (println "Update" (str configuration-file))
    (with-open [w (clojure.java.io/writer configuration-file)]
      (z/print-root zipper w))
    (println "OK")))

(defn get-config
  "Tries to read configs from default locations in this order:

  1. From .prtweeter.edn in the PWD
  2. From $XDG_CONFIG_HOME/prtweeter.edn if XDG_CONFIG_HOME is specified
  3. From ~/.config/prtweeter
  4. Prompt user for missing configuration variables to create an initial configuration
  "
  []
  (let [config-file
        (some (fn [f]
                (println "Looking for configuration in" (str f))
                (when (.exists f) f))
              [local-configuration-file user-configuration-file])]
    (if config-file
      (read-config config-file)
      (create-new-config-interactively))))
