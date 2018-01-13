(ns candidate-exercise.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [candidate-exercise.home :as home]
            [candidate-exercise.search :as search]))

(defroutes app
  (GET "/" [] home/page)
  (POST "/search" [] search/page)
  (route/resources "/")
  (route/not-found "Not found"))

(def handler
  (-> app
      (wrap-defaults site-defaults)
      wrap-reload))
