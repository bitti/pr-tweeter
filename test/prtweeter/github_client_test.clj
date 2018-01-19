(ns prtweeter.github-client-test
  (:require [clojure.java.io :refer [resource]]
            [midje.sweet :refer :all]
            [clj-http.client :as http]
            [prtweeter.github-client :as sut]))

(facts "About getting a list of pull requests"
  (fact "Yields"
        (sut/get-pulls
         {
          :github {:github-url "https://somedomain.com/repos"
                   :user "username"
                   :repository "repository"
                   :begin #inst "2016-10-01"
                   }}) =>
        '({
           :assignee "repo-owner"
           :title "bugfixes etc."
           :state "open"
           :html_url "link to PR"
           :body "a new PR"
           :branch "master"
           :created_at #inst "2016-10-20T08:36:10Z"
           :login "requestor"
           :repo "Repo"})
        (provided
         (http/get "https://somedomain.com/repos/username/repository/pulls" anything) =>
         {:status 200
          :body [
                 ;; Old PR which should be filtered out
                 {
                  :created_at "2016-09-30T08:36:10Z"
                  :user { :login "requestor" }
                  :base { :repo { :description "Repo" } :ref "master" }
                  :assignee { :login "repo-owner" }
                  }

                 ;; We should see this
                 {
                  :html_url "link to PR"
                  :state "open"
                  :body "a new PR"
                  :title "bugfixes etc."
                  :created_at "2016-10-20T08:36:10Z"
                  :user { :login "requestor" }
                  :base { :repo { :description "Repo" } :ref "master" }
                  :assignee { :login "repo-owner" }
                  }
                 ]
          })
        ))
