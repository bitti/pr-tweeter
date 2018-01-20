(ns prtweeter.core
  (:gen-class)
  (:require [clojure.string :refer [join]]
            [clojure.tools.cli :refer [parse-opts summarize]]
            [prtweeter.config-handler :refer [get-config update-earliest-pr!]]
            [prtweeter.helper :refer :all]
            [prtweeter.twitter-client :as twitter]
            [prtweeter.github-client :as github]
            [selmer.filters :refer [add-filter!]]
            [selmer.parser :refer [render]]
            [selmer.util :refer [without-escaping]]))

;; Not part of the selmer standard filters, therefore we roll our own
(add-filter! :abbreviate
             (fn [str maxlength suffix]
               (prtweeter.helper/abbreviate str (Integer/valueOf maxlength) suffix)))

(defn format-tweet [template attributes]
  (without-escaping
   (render template attributes)))

(defn- tee [f seq] (f seq) seq)

(defn- warn-about-limit [pr-limit seq]
  (tee
   (fn [seq]
     (let [c (count seq)]
       (when (> c pr-limit)
         (println "Found" c "new PRs but will only publish the configured limit of" pr-limit "per run"))))
   seq))

(defn- get-tweeter
  "Returns a reducing function which conditionally tweets as a side
  effect by an optional confirmation and returns the PR creation date"
  [config confirm]
  (fn [_ pr]
    (let [formatted-tweet (format-tweet (config :status-template) pr)]
      (println "Tweeting:" formatted-tweet)
      (if (or (not confirm)
              (re-matches  #"[yY]|" (prompt "Do you want to publish this tweet? (y=yes, n=skip) [y]")))
        (abort-on-error twitter/default-error
          (twitter/status-update (config :twitter) formatted-tweet)))
      (:created_at pr)
      )))

(def cli-options
  [["-i" nil "Interactive mode to confirm or skip individual tweets"
    :id :interactively
    :default false]
   ["-h" "--help"]]
  )

(defn- parse-options [args]
  (parse-opts
   args cli-options
   :summary-fn
   #(str "prtweeter - Tweet new github pull requests\n\nValid options:\n" (summarize %))
   ))

(defn -main
  [& args]
  (let [options (parse-options args)]
    (cond
      (:errors options)
      (abort (str (join "\n" (:errors options)) "\n\n" (:summary options)))

      (:help (:options options))
      (println (:summary options))

      :else
      (let [confirm (:interactively (:options options))
            config (get-config)
            pr-limit (config :pr-limit-per-run)
            earliest-pr (config :earliest-pr)]
        (->>
         (github/get-pulls (get-in config [:github :user])
                           (get-in config [:github :repository])
                           earliest-pr)
         reverse ; Newest PRs are listed first, but we want to publish the oldest first
         (warn-about-limit pr-limit)
         (take pr-limit)
         (reduce (get-tweeter config confirm) earliest-pr)
         (update-earliest-pr! config)
         ))))
  ;; Not sure why, but this is necessary after a
  ;; twitter/account-verify-credentials call, even though there are no running agents left
  (System/exit 0)
  )
