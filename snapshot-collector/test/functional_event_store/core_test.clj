(ns functional-event-store.core-test
  (:require [midje.sweet :refer :all]
            [functional-event-store.core :refer :all]))

(facts "about events"
       (fact (new-customer-event "name" "email") => {:event-type :new-customer
                                                     :name       "name"
                                                     :email      "email"} )
       (fact (new-customer-email-event "a@b.com") => {:event-type :new-customer-email
                                                      :email      "a@b.com" }))

; tag::new-customer-event[]
(facts "about new customer events"
       (fact "customers can have just a name"
             (new-customer-event "fred") => {:event-type :new-customer
                                             :name       "fred"
                                             :email      nil})
       (fact "customers can start with name and email"
             (new-customer-event "fred" "a@b") => {:event-type :new-customer
                                                   :name       "fred"
                                                   :email      "a@b"}))
; end::new-customer-event[]

(facts "about handlers"
       (fact "Updates defined entity fields"
             (handle-event {:name nil} (new-customer-name-event "foo") handler-map) => {:name "foo"})
       (fact "leaves entity unchanged if field not present in the entity"
             (handle-event {:name "foo"} (new-customer-email-event "a@b") handler-map) => {:name "foo"})
       (fact "leaves empty entity unchanged"
             (handle-event {} (new-customer-email-event "a@b") handler-map) => {}))
; tag::hydrate-entity[]
(facts "about building entities"
       (fact "entity state represents the latest event states"
             (let [events [(new-customer-event "graham")
                           (new-customer-name-event "Graham")
                           (new-customer-email-event "a@b")
                           (new-customer-email-event "b@c")]]
               (hydrate-entity {:name nil :email nil} events) => {:name  "Graham"
                                                                  :email "b@c"})))
; end::hydrate-entity[]
(facts "about event store"
       (fact "can add event to empty store"
             (add-event :event) => [:event])
       (fact "can add event occupied storeempty store"
             (add-event [:event1] :event2) => [:event1 :event2]))

(facts "about event processing"
       (fact (defaggregate {} => {}))
       (fact (defaggregate {:name nil} => {:name nil}))
       (fact (defaggregate {:name nil :email nil}) => {:name  nil
                                                       :email nil})
       (fact (new-customer-event "a" "b") => {:email      "b",
                                              :name       "a",
                                              :event-type :new-customer})
       
       (fact "event does not add fields"
             ; tag::sparse-aggregate[]
             (let [aggregate (defaggregate {:name nil})]
               (handle-new-customer-event aggregate (new-customer-event "Graham Brooks" "graham@grahambrooks.com")) => {:name "Graham Brooks"}))
                                        ; end::sparse-aggregate[]
       ; tag::full-aggregate[]
       (fact "event changes all event fields"
             (let [aggregate (defaggregate {:name nil :email nil})]
               (handle-new-customer-event aggregate (new-customer-event "Graham Brooks" "graham@grahambrooks.com")) => {:name "Graham Brooks" :email "graham@grahambrooks.com"}))
                                        ; end::full-aggregate[]
       
       (fact
        (let [customer {:name "foo" :email "bar"}]
          (handle-new-customer-email customer (new-customer-email-event "a@b.com")) => {:name  "foo"
                                                                                        :email "a@b.com"})))

