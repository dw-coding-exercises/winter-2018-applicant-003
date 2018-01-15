(ns candidate-exercise.election-search
  (:require [hiccup.page :refer [html5]]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.string :as str])
  (:import (java.text SimpleDateFormat)))

(defprotocol ISearchElections
  "A component for searching for upcoming elections.

  The primary purpose of having this as a component is to easily allow for
  switching between environments. In addition, it makes it possible to have
  long-lived state (e.g. persistent TCP connections, circuit breakers,
  monitoring, etc.)"

  (elections [this district-divisions]
    "Given a list of district divisions, return the upcoming elections."))

(defrecord TurboVoteElection [url])

(extend-type TurboVoteElection
  ISearchElections
  ;; pass in url, so this can easily be used in other envs (e.g. staging)
  (elections [{:keys [url]} district-divisions]
    (edn/read-string
      (:body
        (http/get url
                  {:socket-timeout 5000
                   :conn-timeout 5000
                   :query-params {"district-divisions" (str/join ","
                                                                 district-divisions)}})))))

(defprotocol IDistrictDivs
  "A component for finding district divisions.

  The primary purpose of having this as a component is to easily allow for
  switching between environments. In addition, it makes it possible to have
  long-lived state (e.g. persistent TCP connections, circuit breakers,
  monitoring, etc.)

  In addition, we have multiple implementations of this component: one that
  queries Google Civic Info API, and one that naively computes what it can
  from the address."

  (district-divisions [this address]
    "Given an address of the form:

    {:candidate-exercise.election-search/street \"street\"
     :candidate-exercise.election-search/street-2 \"street 2\"
     :candidate-exercise.election-search/city  \"my city\"
     :candidate-exercise.election-search/state \"AK\"
     :candidate-exercise.election-search/zip \"12345\"}

    Responds with the list of all applicable district divisions."))

(defrecord GoogleCivicInfoDistrictDivs [api-key])

(extend-type GoogleCivicInfoDistrictDivs
  IDistrictDivs
  (district-divisions [{:keys [api-key]}
                       {:keys [::street
                               ::street-2
                               ::city
                               ::state
                               ::zip]}]
    (let [resp (http/get "https://www.googleapis.com/civicinfo/v2/representatives"
                         {:socket-timeout 5000
                          :conn-timeout 5000
                          :query-params {"address" (str/join " "
                                                             [street
                                                              street-2
                                                              city
                                                              state
                                                              zip])
                                         "includeOffices" false
                                         "key" api-key}})]
      (keys (get (json/decode (:body resp))
                 "divisions")))))

(defrecord DefaultDistrictDivs [])

(extend-type DefaultDistrictDivs
  IDistrictDivs
  (district-divisions [this {:keys [::city
                                    ::state]}]
    (let [base "ocd-division/country:us"
          state-ocd (when (not (str/blank? state))
                      (str base "/state:" (str/trim (str/lower-case state))))]
      (concat (when state-ocd
                [state-ocd])
              (when (and state-ocd
                         (not (str/blank? city)))
                [(str state-ocd "/place:" (-> city
                                              str/trim
                                              str/lower-case
                                              (str/replace #"\s" "_")))])))))

(defn form->address
  "Turn a form from `candidate-exercise.home/address-form` into an address
  entity that can be sued by `district-divisions`."
  [form]
  (set/rename-keys form
                   {"street" ::street
                    "street-2" ::street-2
                    "city" ::city
                    "state" ::state
                    "zip" ::zip}))

(defn page [district-divs
            search-elections
            {form :form-params}]
  (try
    (let [fmt (SimpleDateFormat. "MM/dd/yy z")
          els (elections search-elections
                         (district-divisions district-divs
                                             (form->address form)))]
      (html5
        [:head
         [:meta {:charset "UTF-8"}]
         [:meta {:name "viewport"
                 :content "width=device-width, initial-scale=1.0, maximum-scale=1.0"}]
         [:title "Find my next election"]
         [:link {:rel "stylesheet" :href "default.css"}]]
        [:body
         (if (seq els)
           [:div
            [:p "Your upcoming elections are:"]
            (vec (concat [:ul]
                         (map (fn [{:keys [description
                                           website
                                           date]}]
                                [:li
                                 description
                                 " - "
                                 (.format fmt date)
                                 " - "
                                 [:a {:href website}
                                  "Website"]])
                              els)))]
           [:div
            [:p "You have no upcoming elections! Please check again later!"]])
         (when (instance? DefaultDistrictDivs district-divs)
           [:div [:p "HINT: You'll get better results if you restart the server with a Google Civic Info API Key!"]
            [:div [:span "For example, start the server with:"]]
            [:div [:code "> GOOGLE_CIVIC_INFO_API_KEY=\"$MY_API_KEY\" lein ring server"]]])]))
    (catch Exception e
      (html5 [:head
              [:meta {:charset "UTF-8"}]
              [:meta {:name "viewport"
                      :content "width=device-width, initial-scale=1.0, maximum-scale=1.0"}]
              [:title "Find my next election"]
              [:link {:rel "stylesheet" :href "default.css"}]]
             [:body
              [:div
               ;; todo: Differentiate between different error types!

               ;; We want to inform the user exactly what's wrong and how to
               ;; address it.
               [:p "There was an error processing your request. Please ensure "
                "that your address is correct. If the problem persists, please "
                [:a {:href "mailto:webmaster@democracy.works"}
                 "contact us"]
                "."]]]))))
