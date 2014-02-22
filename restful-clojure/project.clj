(defproject restful-clojure "0.1.0-SNAPSHOT"
  :description "An example RESTful shopping list application back-end written in Clojure to accompany a tutorial series on kendru.github.io"
  :url "https://github.com/kendru/restful-clojure"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.2.1"]
                 [ring/ring-jetty-adapter "1.2.1"]
                 [compojure "1.1.6"]]

  ; The lein-ring plugin allows us to easily start a development web server
  ; with "lein ring server". It also allows us to package up our application
  ; as a standalone .jar or as a .war for deployment to a servlet contianer
  ; (I know... SO 2005).
  :plugins [[lein-ring "0.8.10"]]

  ; See https://github.com/weavejester/lein-ring#web-server-options for the
  ; various options available for the lein-ring plugin 
  :ring {:handler restful-clojure.handler/app
         :nrepl {:start? true
                 :port 9998}}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
