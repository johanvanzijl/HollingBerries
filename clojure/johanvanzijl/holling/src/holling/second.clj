(ns holling.second
  (:use [clojure.java.io :only [reader delete-file]]
        [clojure.data.csv :only [read-csv]]
        [clj-time.core :only [plus days]]
        [clojure.math.numeric-tower :only [ceil]]
        [clj-time.format :only [formatter parse unparse]]
        ))

(defrecord Suppinfo [ suppcode sellby markup-perc markup-curr roundup ])
(def suppliers [ (Suppinfo. "32" -3 0 -2 false)  ;Susan Windler (Supplier ID 32)
                 (Suppinfo. "101" -3 0 -2 false) ;Togetherness Tshabalala (Supplier ID 101)
                 (Suppinfo. "219" 0 0.1 0 true ) ;Promise Mashangu (Supplier ID 219)
                 (Suppinfo. "204" 0 0.1 0 true ) ;Karel Visser (Supplier ID 204)
                ])

(defrecord Prodtype [codefrom codeto sellby markup])
(def prodtypes [ (Prodtype. 1000 1099 7 0.5)   ;Other Fruit 1
                 (Prodtype. 1100 1199 14 0.4)  ;Apples
                 (Prodtype. 1200 1299 5 0.35)  ;Bananas
                 (Prodtype. 1300 1399 7 0.55)  ;Berries
                 (Prodtype. 1400 1999 7 0.5)   ;Other Fruit 2
               ])

(defrecord ProductRec [suppcode prodcode desc deldate cost qty] )

(def out-file "pricefile2.txt")
(def in-file "produce.csv")

(defn parse-product [item]
   "Parses each line of the input file to a ProductRec"
   (ProductRec.  (item 0) ;supplier code
                 (Integer/parseInt (item 1)) ;parse the prodcode to int
                 (item 2) ;description
                 (parse (formatter "yyyy/MM/dd") (item 3))  ;parse delivery date
                 (/ (Integer/parseInt (item 4)) 100)  ;parse cost
                 (Integer/parseInt (item 5)))) ;parse qty
   
(def rdr (reader in-file))   ;Define the reader here as I need to keep stream open
(def products 
     "List of ProductRec. Lazy read of input file"
     (for [ item (drop 1 (line-seq rdr))]   ;line-seq returns lazySeq per line
                     (first (read-csv item)))) 

(defn between? [n min max]
  (and (>= n min) (<= n max)))

(defn get-prodtype [prodcode prodtypes] 
        "Searches for a product type in prodtypes for given prodcode"
        (first (for [prodtype prodtypes :when (let [{codefrom :codefrom codeto :codeto} prodtype]
                                              (between? prodcode codefrom codeto))] prodtype)))

(defn get-suppinfo [suppcode suppliers] 
        "Searches for supplier info in suppliers for given suppcode. May return nil if no supplier info provided"
        (first (filter (comp(partial = suppcode) :suppcode) suppliers)))
  
(defn calc-product [product] 
   "Performs the price calculation for a single ProductRec. Assoc's Sales Price and Expiry Date into the ProductRec"
   (let [suppinfo     (get-suppinfo (:suppcode product) suppliers)
         prodtype     (get-prodtype (:prodcode product) prodtypes) ]
      ( -> product
        (assoc :price (if (nil? suppinfo)
                        (* (:cost product) (+ (:markup prodtype) 1))
                        (let [rawprice (+ (* (:cost product) (+ (:markup prodtype) (:markup-perc suppinfo) 1))
                                          (:markup-curr suppinfo))]
                             (if (> rawprice 0) 
                               (if (:roundup suppinfo) (ceil rawprice) rawprice)
                               0 ))))
        (assoc :expdate (plus (:deldate product) (days (:sellby prodtype)) 
                                                 (when suppinfo (days (:sellby suppinfo))))))))               
   
(defn label-product [product]
   "Generate the label for a single ProductRec"
   (apply str 
     ( repeat (:qty product) 
       (str
         "R" (format "%8.2f" (double (:price product)))
         (unparse (formatter "yyyy/MM/dd") (:expdate product))
         (subs (:desc product) 0 31)
         "\n"))))

(defn -main []
    (delete-file out-file true)  ;silently delete output file, as we are now spitting in append mode.
    (doall (for [product products] 
                (spit out-file 
                      (label-product (calc-product (parse-product product))) :append true)))
    (. rdr close)) ;ensure the reader is closed

