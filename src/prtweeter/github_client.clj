(ns prtweeter.github-client
  (:require [clj-http.client :as http]
            [prtweeter.helper :refer :all]))

(def ^:const github-api-url "https://api.github.com/repos")

(defn- query-pulls [user repository]
  (http/get (join-once \/ github-api-url user repository "pulls")
            {:as :json
             :accept "application/vnd.github.v3+json"}))

(defn get-repository-info
  "Get basic information about a given repository"
  [user repository]

  ;; See https://developer.github.com/v3/repos/#get
  (http/get (join-once \/ github-api-url user repository)
            {:as :json
             :accept "application/vnd.github.v3+json"}))

(defn get-pulls
  "Pull list of open pull requests of a given repo by start date"
  [user repository start]
  (let [response

        ;; See https://developer.github.com/v3/pulls/#list-pull-requests
        (:body (query-pulls user repository))]
    (->> response

         ;; Transform the created_at fields into actual 'inst' instances
         (map #(update % :created_at clojure.instant/read-instant-date))

         ;; Filter by 'begin' cutoff date
         (filter #(> 0 (compare start (:created_at %))))

         ;; extract only relevant fields
         (map #(merge (select-keys % [:html_url :state :created_at :body :title])
                      { :login (get-in % [:user :login])}
                      { :descr (get-in % [:base :repo :description])}
                      { :name (get-in % [:base :repo :name])}
                      { :base (get-in % [:base :ref]) }
                      { :assignee (get-in % [:assignee :login])}
                      )))))
