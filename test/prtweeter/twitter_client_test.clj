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

  (fact "Other errors throw exceptions"
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

(def verified-credentials-answer
  {:headers :some-headers
   :status {:code 200, :msg "OK", :protocol "HTTP/1.1", :major 1, :minor 1},
   :body :some-json-parsed-answer})

(facts "About verify credentials"

  (fact "Returns answer on status code 200"
    (sut/verify-credentials (gen/generate (s/gen :prtweeter.config-handler/twitter)))
    => verified-credentials-answer

    (provided
     (twitter.callbacks.handlers/response-return-everything anything) => verified-credentials-answer)
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

      ;; Note: of course a successful requests doesn't return an error
      ;; code, but we want to test here that only HTTP status codes trigger errors
      :body ((promise) "{ \"code\": 187, \"errors\": [\"Duplicate message error message\"]}")
      }
     ))

  (fact "Throws exception on error code"
    (sut/verify-credentials (gen/generate (s/gen :prtweeter.config-handler/twitter)))
    => (throws "verify credentials error")

    (provided
     (http.async.client.request/execute-request
      anything anything
      :status anything
      :headers anything
      :part anything
      :completed anything
      :error anything) =>
     {:status ((promise) {:code 403})
      :error ((promise) (ex-info "verify credentials error" {}))
      :done ((promise) true)
      :body ((promise) "{ \"code\": 187, \"errors\": [\"Duplicate message error message\"]}")
      }
     ))
    )
