(ns prtweeter.twitter-client
  (:require [twitter.api.restful :as twitter]
            [twitter.callbacks.handlers
             :refer
             [exception-rethrow response-return-body response-throw-error]]
            [twitter.oauth :as twitter-oauth])
  (:import twitter.callbacks.protocols.SyncSingleCallback))

(defn default-error [e]
  (str "\nEncountered a problem when contacting twitter:\n\n" e
       "\n\nPlease check the provided credentials and your network connection."))

(defn- credentials->oauth [credentials]
  (apply twitter-oauth/make-oauth-creds
         ((juxt :consumer-key
                :consumer-secret
                :access-token
                :access-token-secret)
          credentials)))

(defn verify-credentials [credentials]
  (twitter/account-verify-credentials :oauth-creds (credentials->oauth credentials)))

(defn skip-duplicates-callback [response]
  (if-let [duplicate-error
           (some (fn [error] (and (= (error "code") 187) error))
                 ((clojure.data.json/read-str (str @(:body response))) "errors"))]
    (printf "Got '%s' Skipping\n" (duplicate-error "message"))
    (response-throw-error response)))

(defn status-update [credentials text]
  (twitter/statuses-update
   :oauth-creds (credentials->oauth credentials)
   :params {:status text}
   :on-exception (fn [response] (print "GOT" response))
   :callbacks (SyncSingleCallback.
               response-return-body
               skip-duplicates-callback
               exception-rethrow)
  ))
