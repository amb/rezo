(ns amb.synth.engine
  (:use clojure.contrib.seq-utils))

(def synth-params {:sample-rate 44000.0, :BPM 120})

; -- FIXME: proper documentation for these macros
(defmacro daset 
  "Set a list of values of a double array. [array [idx val] [idx val] ...]
   if <a> is a s-exp it is evaluated at each iteration."
  [a vals]
  `(do ~@(for [[idx val] (partition 2 vals)]
	   `(aset ~(with-meta a {:tag 'doubles}) ~idx (double ~val)))))

(defmacro dalet
  "binds variables to array of doubles values inside a let" 
   [a vals & body]
  `(let [~@(mapcat (fn [[ito ifr]]
                     [ito `(aget ~(with-meta a {:tag 'doubles}) ~ifr)])
                   (partition 2 vals))] ~@body))

;-- oscillator "class"
(def osc-type-sin (fn [x] (Math/sin (* x Math/PI))))
(def osc-type-saw (fn [x] x))
(def osc-type-sqr (fn [x] (if (> x 0.5) 1.0 0.0)))

(def osc-enum {:freq 0, :phase 1, :volume 2, :out 3})

;-- horrible, horrible, horrible. find ways to make this more readable
(defn do-osc [#^doubles o func]
  (dalet o [fr 0, ph 1, vl 2]
    (let [re (double (* (func ph) vl))]
      (daset o [1 (- (+ ph fr) (Math/floor ph))
		3 re])))) 
; aset returns re so we don't have to do it separately

;-- synth "class"
(def synth-enum {:note-pitch 0, :car-vol 1, :mod-freq 2, :mod-vol 3})
(def constm (double-array 4))
(daset constm [0 0.005, 1 1.0, 2 0.00666, 3 0.00505])

(def osc-mod (double-array 4))
(def osc-car (double-array 4))

(defn set-note [fr]
  (aset constm 0 fr)
  (aset constm 2 (* 0.5 fr)))

(def osc-1-func (atom osc-type-sin))
(def osc-2-func (atom osc-type-sin))

(defn do-synth []
  (dalet osc-mod [os1-o 3]
    (dalet constm [car-f 0, car-v 1, mod-f 2, mod-v 3]
      (daset osc-mod [0 (* car-f mod-f), 2 (* car-f mod-v)])
      (daset osc-car [0 (+ car-f os1-o), 2 car-v]) 
      (do-osc osc-mod @osc-1-func)
      (do-osc osc-car @osc-2-func))))
