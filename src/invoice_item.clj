(ns invoice-item)

(defn- discount-factor [{:invoice-item/keys [discount-rate]
                         :or                {discount-rate 0}}]
  (- 1 (/ discount-rate 100.0)))

(defn subtotal
  [{:invoice-item/keys [precise-quantity precise-price discount-rate]
    :as                item
    :or                {discount-rate 0}}]
  (* precise-price precise-quantity (discount-factor item)))

(defn loadInvoice (clojure.edn/read-string (slurp "../invoice.edn")))

(defn hasConditionsFirstFilter
  [{rate :tax/rate cat :retention/category }]
  (and
   (= rate 19)
   (not (= cat :ret_fuente ))
   )
  )

(defn get-conditions
  [item]
  (concat
   (get item :taxable/taxes)
   (get item :retentionable/retentions)
    )
  )


;; (defn filter-invoices
;;  [invoice]
;;  (filter #(some has19percentIVA (get-taxable-taxes %)) (get invoice :invoice/items))
;;  )

(defn filter-invoice-items
  [invoice]
  (->> (get invoice :invoice/items)
       (filter #(hasConditionsFirstFilter (get-conditions %)))
               ))