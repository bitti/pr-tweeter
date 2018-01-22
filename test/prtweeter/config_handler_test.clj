(ns prtweeter.config-handler-test
  (:require [clojure.pprint :as pp]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [midje.sweet :refer :all]
            [prtweeter.config-handler :as sut]))

(facts "About udpate-earliest-pr!"
  (with-out-str
    (fact "outputs status messages"
      (with-open [w (java.io.StringWriter.)]
        (let [testconfig (gen/generate (s/gen :prtweeter.config-handler/config-spec))
              testdate (gen/generate (s/gen :prtweeter.config-handler/earliest-pr))
              updated-config (assoc testconfig :earliest-pr testdate)
              comment ";; A random comment\n"]
          (fact "updates :earliest-pr in place"
            (sut/update-earliest-pr! (with-meta testconfig {:file "filename"}) testdate) => nil

            (provided (slurp "filename") => (str comment (pr-str testconfig)))
            (provided (clojure.java.io/writer "filename") => w))
          (str w) => (str comment (pr-str updated-config))))
      )) => "Update filename\nOK\n")

(facts "About get-config"
  (with-redefs
    [sut/local-configuration-file (proxy [java.io.File] ["first location"] (exists [] false))
     sut/user-configuration-file (proxy [java.io.File] ["second location"] (exists [] false))]
    (with-out-str
      (fact "starts interactive config when config file is not found"
        (sut/get-config) => :new-interactive-config

        (provided (#'sut/create-new-config-interactively) => :new-interactive-config)
        ))
    => "Looking for configuration in first location\nLooking for configuration in second location\n"
    ))
