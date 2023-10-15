(ns invoice-item
  (:require [clojure.data.json :as json]
            [clojure.string :as str])
  (:import (java.sql Date)))

(use 'clojure.test)

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
  [{rate :tax/rate cat :retention/category taxCat :tax/category}]
  (and
    (= taxCat :iva)
    (= rate 19)
    (not (= cat :ret_fuente))
    )
  )

(defn hasConditionsSecondFilter
  [{cat :retention/category taxCat :tax/category retRate :retention/rate}]
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
;; Function to parse the invoice in the project and return a Clojure map
(defn reformat-date
  [date]
  (let [[dd mm yyyy] (str/split date #"/")]
    (str yyyy "-" mm "-" dd)
    )
  )
(defn value-parser [key value]
  (cond
    (or (= key :items) (= key :taxes) (= key :customer) (= key :retentions) (number? value)) value
    (or (= key :issue-date) (= key :payment_date)) (Date/valueOf (reformat-date value))
    :else (str value)
    )
  )

(defn parse-json-to-map
  [filename]
  (json/read-str (slurp filename) :key-fn #(keyword %) :value-fn value-parser)
  )
(def jsonInvoice (parse-json-to-map "../invoice.json"))

;; The next code concerns the PROBLEM 3 and cover the subtotal function with unit tests
;
(deftest subtotalTest
  (is (= (subtotal {  :invoice-item/precise-quantity 2 :invoice-item/precise-price 2000 :invoice-item/discount-rate 60 }  ) 1600.0))
  (is (= (subtotal {  :invoice-item/precise-quantity 0 :invoice-item/precise-price 0 :invoice-item/discount-rate 60 }  ) 0.0))
  (is (= (subtotal {  :invoice-item/precise-quantity 0 :invoice-item/precise-price 1000 :invoice-item/discount-rate 60 }  ) 0.0))
  (is (= (subtotal {  :invoice-item/precise-quantity 1 :invoice-item/precise-price 1000 :invoice-item/discount-rate 0 }  ) 1000.0))
  (is (= (subtotal {  :invoice-item/precise-quantity 2 :invoice-item/precise-price 1000.2 :invoice-item/discount-rate 0 }  ) 2000.4))
  )