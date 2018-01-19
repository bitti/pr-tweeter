(ns prtweeter.core-test
  (:require [clojure.test :refer :all]
            [prtweeter.config-handler :refer [default-template]]
            [prtweeter.core :refer :all]))

(defmacro ** [s exp]
  (apply str (repeat exp s)))

(deftest tweet-formatting
  (testing "default template"
    (is (= (format-tweet default-template
            {
             :name "github"
             :login "david"
             :created_at #inst "2018-01-02"
             :html_url "https://something"
             :title "A major improvement"
             })
           "github PR by david on 1/2/18 08:00 CST: A major improvement https://something"
           ))
    (is (= 140
           (count
            (format-tweet default-template
             {
              :name (** "100-chars-" 10) ; Maxium repo name length on github
              :login (** "X" 39)         ; Maxium user login name
              :created_at #inst "2018-01-02"
              :html_url (** "x" 23)     ; Fixed effective length for URLs on twitter
              :title (** "1000-chars" 100)
              }))))
    ))
