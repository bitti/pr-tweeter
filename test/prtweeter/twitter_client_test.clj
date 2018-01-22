(ns prtweeter.twitter-client-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [midje.sweet :refer :all]
            [prtweeter.config-handler :refer :all]
            [prtweeter.twitter-client :as sut]))

(facts "About status-update"

  (fact "Error code 187 doesn't throw exception and prints message"
    (sut/skip-duplicates-callback
     {:status ((promise) { :code 403 })
      :body ((promise) "{ \"errors\": [ { \"code\": 187, \"message\": \"Status is a duplicate.\" } ] }")
      }) => nil
    (provided (clojure.core/printf #"Skipping" anything) => nil))

  (fact "Other errors throw exeptions"
    (sut/skip-duplicates-callback
     {:status ((promise) { :code 403 })
      :body ((promise)
             "{ \"request\": \"faulty-request\" \"errors\": [ { \"code\": 123, \"message\": \"Something else.\" } ] }")
      }) => (throws "twitter error")
    (provided (twitter.callbacks.handlers/response-throw-error anything)
              => (throw (ex-info "twitter error" {}))))

  (fact "No exceptions on HTTP code 200"
    (sut/status-update (gen/generate (s/gen :prtweeter.config-handler/twitter))
                       "A random message") => :some-json-parsed-answer
    (provided
     (twitter.callbacks.handlers/response-return-body anything) => :some-json-parsed-answer)
    (provided
     (http.async.client.request/execute-request
      anything anything
      :status anything
      :headers anything
      :part anything
      :completed anything
      :error anything) =>
     {:status ((promise) {:code 200})
      :error (promise)
      :done ((promise) true)
      :body ((promise) "{ \"code\": 187, \"errors\": [\"Duplicate message error message\"]}")
      }
     )))
