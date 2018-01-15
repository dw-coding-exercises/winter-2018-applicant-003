(ns candidate-exercise.core
  (:require [compojure.core :as cmpj]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [candidate-exercise.home :as home]
            [candidate-exercise.election-search :as search]))


(defn my-routes [district-divs
                 search-elections]
  (cmpj/routes
    (cmpj/GET "/" [] home/page)
    (cmpj/POST "/search" [] (partial search/page
                                     district-divs
                                     search-elections))
    (route/resources "/")
    (route/not-found "Not found")))

(def handler
  (-> (my-routes (if-let [k (System/getenv "GOOGLE_CIVIC_INFO_API_KEY")]
                   (search/->GoogleCivicInfoDistrictDivs k)
                   (search/->DefaultDistrictDivs))
                 (search/->TurboVoteElection "https://api.turbovote.org/elections/upcoming"))
      (wrap-defaults site-defaults)
      wrap-reload))
