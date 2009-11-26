(ns amb.synth.audioloop-rt
  (:use clojure.contrib.seq-utils)
  (:import [org.jrtaudio JRtAudio]))

; -- output
(def dl (atom nil))
(def running (atom false))

(gen-class 
 :name amb.synth.audioloop-rt.impl
 :methods [[callback [] Integer]])

(defn -callback [this] 0)

; audio-renderer is seen as a function that returns a value between -1.0 to 1.0
; for each of its invocation
(defn eng-start [audio-renderer]
  (reset! dl (let [jrt (JRtAudio. (new amb.synth.audioloop-rt.impl))]
	       (.openStreamOut jrt)
	       (.startStream jrt)
	       jrt))
  (reset! running true))

(defn eng-stop []
  (reset! running false)
  (if (not (nil? @dl))
    (.closeStream @dl)))

