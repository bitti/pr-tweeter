(ns prtweeter.twitter-client
  (:require [prtweeter.helper :refer [abort]]
            [twitter.api.restful :as twitter]
            [twitter.oauth :as twitter-oauth]))

(defmacro abort-on-error [body]
  `(try
     ~body
     (catch Exception e#
       (binding [*out* *err*]
         (println "\nEncountered a problem when contacting twitter:\n\n" (str e#)
                  "\n\nPlease check the provided credentials and your network connection.")
         (abort e#)
         ))))

(defn- credentials->oauth [credentials]
  (apply twitter-oauth/make-oauth-creds
         ((juxt :consumer-key
                :consumer-secret
                :access-token
                :access-token-secret)
          credentials)))

(defn user-info [credentials]
  (twitter/users-show :oauth-creds (credentials->oauth credentials)))

(defn status-update [credentials text]
  (twitter/statuses-update
   :oauth-creds (credentials->oauth credentials)
   :params {:status text})
  )
