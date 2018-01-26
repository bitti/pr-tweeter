(defproject prtweeter "0.1.0"
  :description "Tweet new pull requests in Github"
  :url "https://github.com/bitti/pr-tweeter"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [clj-http "3.7.0"]
                 [cheshire "5.8.0"] ;; for :as :json
                 [clojure-future-spec "1.9.0-beta4"]
                 [midje "1.9.1"]
                 [environ "1.1.0"]
                 [twitter-api "1.8.0"]
                 [selmer "1.11.6" :exclusions [joda-time]]
                 [rewrite-clj "0.6.0"]
                 [org.slf4j/slf4j-simple "1.7.12"]
                 ]
  :main ^:skip-aot prtweeter.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["test/resources"]}}
  )
