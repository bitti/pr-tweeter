(ns prtweeter.helper-test
  (:require [clojure.test :refer :all]
            [prtweeter.helper :as sut])
  (:import java.lang.AssertionError))

(deftest join-once
  (testing "common cases"
    (are [generated expected] (= generated expected)
      (sut/join-once \/ "first" "second") "first/second"
      (sut/join-once \/ "first/" "/second") "first/second"
      (sut/join-once \/ "first" "/second") "first/second"
      (sut/join-once \/ "first/" "second") "first/second"
      (sut/join-once \/ "first/" "//second") "first//second"
      (sut/join-once \/ "first//" "/second") "first//second"
      (sut/join-once \/ "first//" "//second") "first///second"
      (sut/join-once \/ "a" "efg" "b" "cd") "a/efg/b/cd"
      (sut/join-once \/ "a/" "/efg" "/b/" "cd/") "a/efg/b/cd/"
      (sut/join-once \/ "/a/" "/efg/" "/b/" "/cd/") "/a/efg/b/cd/"
      ))
  (testing "edge cases"
    (are [generated expected] (= generated expected)
      (sut/join-once \/ "/" "//" "//" "/") "///"
      (sut/join-once \/ "" "") "/"
      (sut/join-once \/ "" "" "" "") "/"
      (sut/join-once \/ "/" "/") "/"
      (sut/join-once \/ "/" "/" "/") "/"
      (sut/join-once \/ "/" "//" "/") "//"
      (sut/join-once \/ "a" "" "" "/" "/b/" "cd") "a/b/cd"
      (sut/join-once \/ "//" "///" "///" "//") "///////"
      (sut/join-once \/ "a//b" "/c/d/" "/d//" "//e") "a//b/c/d/d///e"
    ))
  (testing "other characters"
    (are [generated expected] (= generated expected)
      (sut/join-once \x "a/" "bcdx" "/efgx" "xhijk") "a/xbcdx/efgxhijk"
      (sut/join-once \newline "this\n" "is""\na\n" "test") "this\nis\na\ntest"
      (sut/join-once \… "a…b" "cd" "efg" "…") "a…b…cd…efg…"
      )))

(deftest abbreviate
  (testing "common cases"
    (are [generated expected] (= generated expected)
      (sut/abbreviate "This is a short text" 20 "...") "This is a short text"
      (sut/abbreviate "This is a short text!" 20 "...") "This is a short t..."
      (sut/abbreviate "This is a much longer text" 20 "...") "This is a much lo..."
      (sut/abbreviate "This is a much longer text" 20 "…") "This is a much long…"
      ))
  (testing "edge cases"
    (are [generated expected] (= generated expected)
      (sut/abbreviate "This has an empty suffix" 20 "") "This has an empty su"
      (sut/abbreviate "" 20 "empty text") ""
      (sut/abbreviate "" 20 "") ""
      (sut/abbreviate "And this" 24 "has somewhat long suffix") "And this"
      (sut/abbreviate "And this is a pretty long text" 24 "has somewhat long suffix")
      "has somewhat long suffix"
      ))
  (testing "precondtion"
    (is (thrown? java.lang.AssertionError
                 (sut/abbreviate "And this" 23 "has somewhat long suffix")))))
