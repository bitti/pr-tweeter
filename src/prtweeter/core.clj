(ns prtweeter.core
  (:gen-class)
  (:require [prtweeter.config-handler :refer [get-config update-earliest-pr!]]
            [prtweeter.github-client :refer [get-pulls]]
            [prtweeter.helper :refer [abort]]
            [prtweeter.twitter-client :refer [status-update abort-on-error]]
            [selmer.filters :refer [add-filter!]]
            [selmer.parser :refer [render]]
            [selmer.util :refer [without-escaping]])
  (:import java.lang.Exception))

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
              (= "y" (print "Do you want to publish this tweet? (y=yes, s=skip) [y]")))
        (abort-on-error (status-update (config :twitter) formatted-tweet)))
      (:created_at pr)
      )))

(def cli-options
  [["-i" nil "Interactive mode to confirm or skip individual tweets"
    :default false
    ]]
  )

(defn -main
  [& args]
  (let [confirm false
        config (get-config)
        pr-limit (config :pr-limit-per-run)
        earliest-pr (config :earliest-pr)]
    (->>
     (get-pulls (get-in config [:github :user])
                (get-in config [:github :repository])
                earliest-pr)
     reverse ; Newest PRs are listed first, but we want to publish the oldest first
     (warn-about-limit pr-limit)
     (take pr-limit)
     (reduce (get-tweeter config confirm) earliest-pr)
     (update-earliest-pr! config)
     )))
