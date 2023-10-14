(ns invoice-item)

(defn- discount-factor [{:invoice-item/keys [discount-rate]
                         :or                {discount-rate 0}}]
  (- 1 (/ discount-rate 100.0)))

(defn subtotal
  [{:invoice-item/keys [precise-quantity precise-price discount-rate]
    :as                item
    :or                {discount-rate 0}}]
  (* precise-price precise-quantity (discount-factor item)))

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
  (concat (->> (get invoice :invoice/items)
       (filter #(hasConditionsFirstFilter (get-conditions %)))
               )
          (->> (get invoice :invoice/items)
               (filter #(hasConditionsSecondFilter (get-conditions %)))
               )
          )
  )