(ns invoice-spec
  (:require
    [clojure.spec.alpha :as s])
  (:require [invoice-item :refer :all])
  )

(defn not-blank? [value] (-> value clojure.string/blank? not))
(defn non-empty-string? [x] (and (string? x) (not-blank? x)))

(s/def :customer/name non-empty-string?)
(s/def :customer/email non-empty-string?)
(s/def :invoice/customer (s/keys :req [:customer/name
                                       :customer/email]))

(s/def :tax/rate double?)
(s/def :tax/category #{:iva})
(s/def ::tax (s/keys :req [:tax/category
                           :tax/rate]))
(s/def :invoice-item/taxes (s/coll-of ::tax :kind vector? :min-count 1))

(s/def :invoice-item/price double?)
(s/def :invoice-item/quantity double?)
(s/def :invoice-item/sku non-empty-string?)

(s/def ::invoice-item
  (s/keys :req [:invoice-item/price
                :invoice-item/quantity
                :invoice-item/sku
                :invoice-item/taxes]))

(s/def :invoice/issue-date inst?)
(s/def :invoice/items (s/coll-of ::invoice-item :kind vector? :min-count 1))

(s/def ::invoice
  (s/keys :req [
                :invoice/issue-date
                :invoice/customer
                :invoice/items
                ]))

; (println (s/valid? ::invoice { :invoice/customer { :customer/name "ANDRADE RODRIGUEZ MANUEL ALEJANDRO", :customer/email "cgallegoaecu@gmail.com" } } ))

(println jsonInvoice)
(println (str "is valid: " (s/valid? ::invoice jsonInvoice)))