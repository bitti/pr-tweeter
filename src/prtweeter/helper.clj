(ns prtweeter.helper
  (:require [clojure.pprint :refer [cl-format]]
            [clojure.string :refer [split trim]]))

(defn join-once
  "Joins at least two strings by the given separator but avoids
  repetitions of the separator at the join points. Empty strings or
  strings consisting only of the join character are - in effect -
  ignored. Only single character separators are supported."
  ([^Character sep s1 s2]
   (cond
     (= sep (last s1) (first s2)) (str s1 (subs s2 1))
     (or (= (last s1) sep) (= (first s2) sep)) (str s1 s2)
     :else (str s1 sep s2)))
  ([^Character sep s1 s2 & r]
   (if (empty? (rest r))
     (join-once sep (join-once sep s1 s2) (first r))
     (recur sep (join-once sep s1 s2) (first r) (rest r)))))

(defn word-wrap
  "Wraps the given text along whitespace boundaries by given
  width (defaults to 72 if not supplied)."
  ([text] (word-wrap text 72))
  ([text width]
   ;; See http://cybertiggyr.com/fmt/fmt.pdf#page=4&zoom=auto,-68,245
   (cl-format nil (str "~{~<~%~1," width ":;~A~> ~}")
              (split text #"\s+"))))

(defn prompt [& more]
  (apply print more)
  (flush)
  (trim (read-line)))

(defmacro abort-on-error
  {:style/indent 1}
  [err-msg-fn & body]
  `(try
     ~@body
     (catch Exception e# (abort (~err-msg-fn e#)))))

(defn abort
  "Wrapper for System/exit so it can be easily stubbed in unit tests"
  [message]
  (binding [*out* *err*] (println message))
  (System/exit 1)
  )
