(ns examples.functional)

; START: stack-consuming-fibo
; bad idea
(defn stack-consuming-fibo [n]
  (cond
   (= n 0) 0 ; <label id="code.stack-consuming-fibo.0"/>
   (= n 1) 1 ; <label id="code.stack-consuming-fibo.1"/>
   :else (+ (stack-consuming-fibo (- n 1))    ; <label id="code.stack-consuming-fibo.n"/>
	    (stack-consuming-fibo (- n 2))))) 
; END: stack-consuming-fibo

; START: tail-fibo
; also bad: still consumes stack!
(defn tail-fibo [n]
  (let [fib (fn fib [f-2 f-1 current] ; <label id="code.tail-fibo.args"/>
	      (let [f (+ f-2 f-1)]
		(if (= n current) ; <label id="code.tail-fibo.term"/>
		  f
	       (fib f-1 f (inc current)))))] ; <label id="code.tail-fibo.recur"/>
    (cond
     (= n 0) 0
     (= n 1) 1
     :else (fib 0 1 2)))) ; <label id="code.tail-fibo.basis"/>
; END: tail-fibo

; START: recur-fibo    
; better but not great
(defn recur-fibo [n]
  (let [fib (fn [f-2 f-1 current]
	      (let [f (+ f-2 f-1)]
		(if (= n current)
		  f
	       (recur f-1 f (inc current)))))]  ; <label id="code.recur-fibo.recur"/>
  (cond
   (= n 0) 0
   (= n 1) 1
   :else (fib 0 1 2))))
; END: recur-fibo

; START: fibo-series
; returns series to n 
; still bad (heap-consuming!)
(defn fibo-series [count]
  (let [n (dec count)
	fib (fn [series current] ; <label id="code.fibo-series.series"/>
	      (let [f (+ (series (- current 1)) (series (- current 2)))]
		(if (= current n)
		  (conj series f) ; <label id="code.fibo-series.term"/>
		  (recur (conj series f) (inc current)))))] ; <label id="code.fibo-series.recur"/>
    (cond
     (= n 0) [0] ; <label id="code.fibo-series.0"/>
     (= n 1) [0 1] ; <label id="code.fibo-series.1"/>
     :else (fib [0 1] 2)))) 
; END: fibo-series

; START: lazy-cons-fibo
(defn lazy-cons-fibo []
  ((fn fib [a b] ; <label id="code.lazy-cons.fib"/>
     (lazy-cons a (fib b (+ a b)))) ; <label id="code.lazy-cons.recur"/>
   0 1)) ; <label id="code.lazy-cons.basis"/>
; END: lazy-cons-fibo

; START: head-fibo
; holds the head (avoid!)
(def head-fibo (lazy-cat [0 1] (map + head-fibo (rest head-fibo))))
; END: head-fibo

; START: count-heads-pairs
(defn count-heads-pairs [coll]
  (loop [cnt 0 coll coll] ; <label id="code.count-heads-loop.loop"/>
    (if (empty? coll) ; <label id="code.count-heads-loop.basis"/>
      cnt
      (recur (if (and (= :h (first coll)) (= :h (second coll))) ; <label id="code.count-heads-loop.filter"/>
	       (inc cnt)
	       cnt)
	     (rest coll)))))
; END: count-heads-pairs
(def count-heads-loop count-heads-pairs)

; START: by-pairs
(defn by-pairs [coll]
  (let [pair (take 2 coll)] ; <label id="code.by-pairs.take"/>
    (when (= 2 (count pair)) ; <label id="code.by-pairs.count"/>
      (lazy-cons pair (by-pairs (rest coll)))))) ; <label id="code.by-pairs.lazy"/>
; END: by-pairs

; START: count-heads-by-pairs
(defn count-heads-pairs [coll]
  (count (filter (fn [pair] (every? #(= :h %) pair)) 
		 (by-pairs coll))))
; END: count-heads-by-pairs
(def count-heads-by-pairs count-heads-pairs)

; START: count-if
(use '[clojure.contrib.def :only (defvar)])
(defvar count-if (comp count filter)  "Count items matching a filter")
; END: count-if

; START: count-runs
(defn
 count-runs
 "Count runs of length n where pred is true in coll."
 [n pred coll]
 (count-if #(every? pred %) (partition n 1 coll)))
; END: count-runs

; START: count-heads-by-runs
(defvar count-heads-pairs (partial count-runs 2 #(= % :h))
  "Count runs of length two that are both heads")
; END: count-heads-by-runs
(def count-heads-by-runs count-heads-pairs)

; START: my-odd-even
(declare my-odd? my-even?)

(defn my-odd? [n]
  (if (= n 0)
    false
    (my-even? (dec n))))

(defn my-even? [n]
  (if (= n 0)
    true
    (my-odd? (dec n))))
; END: my-odd-even

; START: parity
(defn parity [n]
  (loop [n n par 0]
    (if (= n 0)
      par
      (recur (dec n) (- 1 par)))))
; END: parity

; START: my-odd-even-parity
(defn my-even? [n] (= 0 (parity n)))
(defn my-odd? [n] (= 1 (parity n)))
; END: my-odd-even-parity

; START: curry
; almost a curry
(defn faux-curry [& args] (apply partial partial args))
; END: curry

; --------------------------------------------------------------------------------------
; -- See www.cs.brown.edu/~sk/Publications/Papers/Published/sk-automata-macros/paper.pdf
; --------------------------------------------------------------------------------------
(defn machine [stream]
   (let [step {[:init 'c] :more
               [:more 'a] :more
               [:more 'd] :more
               [:more 'r] :end
               [:end nil] true}]
     (loop [state :init
            stream stream]
       (let [next (step [state (first stream)])]
         (when next
           (if (= next true)
               true
             (recur next (rest stream))))))))

  
(declare init more end)

(defn init [stream]
  (if (#{'c} (first stream))
    (more (rest stream))))

(defn more [stream]
  (cond 
   (#{'a 'd} (first stream)) (more (rest stream))
   (#{'r} (first stream)) (end (rest stream))))

(defn end [stream]
  (when-not (seq stream) true))


	      