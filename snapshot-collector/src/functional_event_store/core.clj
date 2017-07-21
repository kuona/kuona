(ns functional-event-store.core)

(defmacro defaggregate
  "An aggregate entity has attributes for the current structure - how
  to create new ones and an instance state. The structure meta data
  helps build new instnaces and apply event data to the state."
  [structure]
  structure)

; tag::new-customer-event[]
(defn new-customer-event
  ([name] {:event-type :new-customer
           :name       name
           :email      nil})
  ([name email]
   {:event-type :new-customer
    :name       name
    :email      email}))
; end::new-customer-event[]

(defn new-customer-name-event
  [name]
  {:event-type :new-customer-name
   :name name })

(defn new-customer-email-event
  [email]
  {:event-type :new-customer-email
   :email email })

; tag::default-handler[]
(defn default-handler
  [entity event]
  (merge entity (select-keys event (keys entity))))
                                        ; end::default-handler[]

; tag::handle-new-customer-event[]
(defn handle-new-customer-event
  [customer event]
  (default-handler customer event))
                                        ; end::handle-new-customer-event[]

(defn handle-new-customer-email
  [customer event]
  (default-handler customer event))

(defn handle-new-customer-name
  [customer event]
  (default-handler customer event))

; tag::handler-map[]
(def handler-map
  {:new-customer       handle-new-customer-event
   :new-customer-email handle-new-customer-email
   :new-customer-name  handle-new-customer-name})
; end::handler-map[]
; tag::handle-event[]
(defn handle-event
  [entity event handlers]
  (let [event-type (:event-type event)
        handler    (event-type handlers)]
    (handler entity event)))
; end::handle-event[]

;// tag::hydrate-entity[]
(defn hydrate-entity
  [entity events]
  (if (empty? events) entity (hydrate-entity (handle-event entity (first events) handler-map) (rest events))))
;// end::hydrate-entity[]
(defn add-event
  ([event] [event])
  ([store event] (concat store [event])))
