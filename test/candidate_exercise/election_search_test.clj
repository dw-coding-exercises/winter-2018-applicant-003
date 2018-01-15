(ns candidate-exercise.election-search-test
  (:require [candidate-exercise.election-search :as search]
            [clojure.test :refer :all]))

(deftest default-district-divs
  ;; This is the only functionally pure business logic, so it's the only one I'm
  ;; unit testing. The others would require an integration test if we deemed it
  ;; necessary.
  (let [dds (search/->DefaultDistrictDivs)]
    (testing "It should use the city and state provided to generate 2 OCD-IDs."
      (is (= (search/district-divisions dds
                                        {::search/city "Portland"
                                         ::search/state "OR"})
             ["ocd-division/country:us/state:or"
              "ocd-division/country:us/state:or/place:portland"])))

    (testing "If there is no city, it should return 1 OCD-ID."
      (is (= (search/district-divisions dds
                                        {::search/state "OR"})
             ["ocd-division/country:us/state:or"])))
    (testing "If there is no city, it should return 1 OCD-ID. Even if city is blank (i.e. non-nil)"
      (is (= (search/district-divisions dds
                                        {::search/city "    "
                                         ::search/state "OR"})
             ["ocd-division/country:us/state:or"])))

    (testing "If there is no state, it should return no OCD-IDs."
      (is (= (search/district-divisions dds
                                        nil)
             [])))
    (testing "If there is no state, it should return no OCD-IDs. Even if state is blank (i.e. non-nil)"
      (is (= (search/district-divisions dds
                                        {::search/state "    "})
             [])))

    (testing "If there is no state, it should return no OCD-IDs, even if it has a city!."
      (is (= (search/district-divisions dds
                                        {::search/city "Portland"})
             [])))

    (testing "The only fields that matter are city and state. (w/ city & state)"
      (is (= (search/district-divisions dds
                                        {::search/street "123 SuperAwesome St"
                                         ::search/state "OR"
                                         ::search/city "Portland"
                                         ::search/zip "12345"})
             ["ocd-division/country:us/state:or"
              "ocd-division/country:us/state:or/place:portland"])))
    (testing "The only fields that matter are city and state. (w/o city & state)"
      (is (= (search/district-divisions dds
                                        {::search/street "123 SuperAwesome St"
                                         ::search/zip "12345"})
             [])))

    (testing "It trims its input."
      (is (= (search/district-divisions dds
                                        {::search/city "  Portland  "
                                         ::search/state "  OR  "})
             ["ocd-division/country:us/state:or"
              "ocd-division/country:us/state:or/place:portland"])))

    (testing "It replaces spaces with underscores."
      (is (= (search/district-divisions dds
                                        {::search/city "Pryor Creek"
                                         ::search/state "OK"})
             ["ocd-division/country:us/state:ok"
              "ocd-division/country:us/state:ok/place:pryor_creek"])))))
