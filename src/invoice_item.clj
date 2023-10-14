(ns invoice-item
  (:require [clojure.data.json :as json]
            [clojure.spec.alpha :as s]))

(defn- discount-factor [{:invoice-item/keys [discount-rate]
                         :or                {discount-rate 0}}]
  (- 1 (/ discount-rate 100.0)))

(defn subtotal
  [{:invoice-item/keys [precise-quantity precise-price discount-rate]
    :as                item
    :or                {discount-rate 0}}]
  (* precise-price precise-quantity (discount-factor item)))

;; The next code concerns the PROBLEM 1 which proposes a function that receives
;; an invoice and returns only the items that match with the conditions
;; which are 19 of IVA rate, and 1 of rete fuente, but not both at the time.
;; --------------------------------------------------------------------
(defn hasConditionsFirstFilter
  [{rate :tax/rate cat :retention/category taxCat :tax/category }]
  (and
    (= taxCat :iva)
    (= rate 19)
    (not (= cat :ret_fuente ))
   )
  )

(defn hasConditionsSecondFilter
  [{ cat :retention/category taxCat :tax/category retRate :retention/rate }]
  (and
    (not (= taxCat :iva))
    (= cat :ret_fuente)
    (= retRate 1)
    )
  )

(defn get-fields
  [item]
  (concat
   (get item :taxable/taxes)
   (get item :retentionable/retentions)
    )
  )

(defn filter-invoice-items
  [invoice]
  (concat (->> (get invoice :invoice/items)
       (filter #(hasConditionsFirstFilter (get-fields %)))
               )
          (->> (get invoice :invoice/items)
               (filter #(hasConditionsSecondFilter (get-fields %)))
               )
          )
  )
;; ------------------------------------------------------------------------------------------------

(defn value-parser [key value]
    (if (or (= key :items) (= key :taxes) (= key :customer) (= key :retentions))
      value
      (str value)
      )
  )

(defn parse-json-to-map
  [jsonObj]
  (json/read-str jsonObj :key-fn #(keyword %) :value-fn value-parser)
  )
(def jsonInvoice (parse-json-to-map (slurp "../invoice.json")))
(print jsonInvoice)
