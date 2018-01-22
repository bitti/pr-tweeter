(ns prtweeter.config-handler-test
  (:require [clojure.pprint :as pp]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [midje.sweet :refer :all]
            [prtweeter.config-handler :as sut]))

(facts "About update-earliest-pr!"
  (with-out-str
    (fact "outputs status messages"
      (with-open [w (java.io.StringWriter.)]
        (let [testconfig (gen/generate (s/gen :prtweeter.config-handler/config-spec))
              testdate (gen/generate (s/gen :prtweeter.config-handler/earliest-pr))
              updated-config (assoc testconfig :earliest-pr testdate)
              comment ";; A random comment\n"]
          (fact "updates :earliest-pr in place while preserving whitespace and comments"
            (sut/update-earliest-pr! (with-meta testconfig {:file "filename"}) testdate) => nil

            (provided (slurp "filename") => (str comment (pr-str testconfig)))
            (provided (clojure.java.io/writer "filename") => w))
          (str w) => (str comment (pr-str updated-config))))
      )) => "Update filename\nOK\n")

(defmacro with-files [files & body]
  "Try to remove some cruft from test setup with this macro"
  `(with-out-str
    (with-redefs
      [sut/local-configuration-file (proxy [java.io.File] ["first location"]
                                      (exists [] ~(contains? files :local)))
       sut/user-configuration-file (proxy [java.io.File] ["second location"]
                                     (exists [] ~(contains? files :user)))]
      ~@body)))

(facts "About get-config"
  (with-files #{}
    (fact "starts interactive config when config file is not found"
      (sut/get-config) => :new-interactive-config
      (provided (#'sut/create-new-config-interactively) => :new-interactive-config)))
  => "Looking for configuration in first location\nLooking for configuration in second location\n"

  (with-files #{:user :local}
    (fact "reads from local config if user and local are given"
      (sut/get-config) => :local-config
      (provided (#'sut/read-config sut/local-configuration-file) => :local-config)))
  => "Looking for configuration in first location\n"

  (with-files #{:user}
    (fact "reads user config if only user is given"
      (sut/get-config) => :user-config
      (provided (#'sut/read-config sut/user-configuration-file) => :user-config)))
  => "Looking for configuration in first location\nLooking for configuration in second location\n")
