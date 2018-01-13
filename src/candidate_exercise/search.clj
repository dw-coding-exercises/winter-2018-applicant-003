(ns candidate-exercise.search
  (:require [hiccup.page :refer [html5]]))

(defn page [{{street "street"
              street-2 "street-2"
              city "city"
              state "state"
              zip "zip"} :form-params}]
  (html5
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1.0, maximum-scale=1.0"}]
     [:title "Find my next election"]
     [:link {:rel "stylesheet" :href "default.css"}]]
    [:body [:div
            "I will do great things! Like display your upcoming elections!"
            (pr-str {:street street
                     :street-2 street-2
                     :city city
                     :state state
                     :zip zip})]]))
