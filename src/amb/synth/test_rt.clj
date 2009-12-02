(ns amb.synth.test-rt
    (:import (org.jrtaudio JRtAudio)))

(def au (JRtAudio.))

(println "Warnings " (.showWarnings au))

(def numDevices (.getDeviceCount au))

(doseq [i (range numDevices)] 
  (let [di (.getDeviceInfo au i)]
    (println "Device " i)
    (println (.str di))))

