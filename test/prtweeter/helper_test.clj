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
