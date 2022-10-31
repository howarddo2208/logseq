(ns frontend.components.property.schema
  "Block property's schema management"
  (:require [frontend.ui :as ui]
            [frontend.util :as util]
            [clojure.string :as string]
            [frontend.handler.property :as property-handler]
            [frontend.db :as db]
            [frontend.db.model :as db-model]
            [frontend.mixins :as mixins]
            [rum.core :as rum]
            [frontend.state :as state]
            [goog.dom :as gdom]
            [frontend.search :as search]
            [frontend.components.search.highlight :as highlight]
            [frontend.components.svg :as svg]
            [frontend.modules.shortcut.core :as shortcut]
            [frontend.context.i18n :refer [t]]
            [frontend.handler.notification :as notification]))

(rum/defc property-item
  [k value]
  [:div.grid.grid-cols-5.gap-1.items-center
   [:label.col-span-1.font-medium.py-2 k]
   value])

(rum/defc schema-type
  [entity schema]
  (let [schema-type (:type schema)
        options (map
                  (fn [item] (if (= schema-type (:value item))
                               (assoc item :selected true)
                               item))
                  [{:label "Any" :value "any"}
                   {:label "Number" :value "number"}
                   {:label "Date" :value "date"}
                   {:label "Url" :value "url"}
                   {:label "Object" :value "object"}])]
    (property-item
     (t :schema/type)
     (ui/select options
       (fn [selected]
         (property-handler/set-property-schema! entity :type (string/lower-case selected)))
       "form-select w-32 sm:w-32"))))

(rum/defc schema-number-range
  [entity schema]
  (when (= (:type schema) "number")
    (let [on-submitted (fn [key]
                         (fn [e]
                           (if-let [result (parse-long (util/evalue e))]
                             (property-handler/set-property-schema! entity key result)
                             (notification/show!
                              [:div "A number is needed"]
                              :warning))))]
      (property-item
       (t :schema/number-range)
       [:div.flex.flex-row.items-center
        [:input.form-input.simple-input.block.focus:outline-none.col-span-1
         {:placeholder "Min"
          :default-value (:min schema)
          :on-blur (on-submitted :min)
          :on-key-up (fn [e]
                       (case (util/ekey e)
                         "Enter"
                         ((on-submitted :min) e)
                         nil))}]
        [:div.px-4 "-"]
        [:input.form-input.simple-input.block.focus:outline-none.col-span-1
         {:placeholder "Max"
          :default-value (:max schema)
          :on-blur (on-submitted :max)
          :on-key-up (fn [e]
                       (case (util/ekey e)
                         "Enter"
                         ((on-submitted :max) e)
                         nil))}]]))))

(rum/defc schema-cardinality
  [entity schema]
  (let [multiple? (boolean (:multiple-values? schema))]
    (property-item
     (t :schema/multiple-values)
     (ui/checkbox
      {:checked multiple?
       :on-change (fn []
                    (property-handler/set-property-schema! entity :multiple-values? (not multiple?)))}))))

(rum/defc schema-description
  [entity schema]
  (property-item
   (t :schema/description)
   (let [description (:description schema)
         on-submitted (fn [e]
                        (let [desc (util/evalue e)]
                          (when-not (string/blank? desc)
                            (property-handler/set-property-schema! entity :description desc))))]
     [:input.form-input.simple-input.block.focus:outline-none.col-span-4
      {:placeholder "Optional"
       :default-value description
       :on-blur on-submitted
       :on-key-up (fn [e]
                    (case (util/ekey e)
                      "Enter"
                      (on-submitted e)

                      nil))}])))

(rum/defc schema
  [entity]
  (let [schema (:block/property-schema entity)]
    [:div.property-schema
     (schema-type entity schema)
     (schema-number-range entity schema)
     (schema-cardinality entity schema)
     (schema-description entity schema)

     ;; TODO: icon
     ;; predicates
     ;; integrations
     ]))