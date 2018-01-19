(defproject prtweeter "0.1.0-SNAPSHOT"
  :description "Tweet new pull requests in Github"
  :url "https://github.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-http "3.7.0"]
                 [cheshire "5.8.0"] ;; for :as :json
                 [slingshot "0.12.2"]
                 [com.rpl/specter "1.0.5"]
                 [clojure-future-spec "1.9.0-beta4"]
                 [org.clojure/test.check "0.9.0"] ;; For generators
                 [org.clojure/tools.logging "0.4.0"]
                 [midje "1.9.1"]
                 [environ "1.1.0"]
                 [twitter-api "1.8.0"]
                 [selmer "1.11.5" :exclusions [joda-time]]
                 [rewrite-clj "0.6.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.slf4j/slf4j-simple "1.7.12"]
                 ]
  :main ^:skip-aot prtweeter.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["test/resources"]}}
  )
