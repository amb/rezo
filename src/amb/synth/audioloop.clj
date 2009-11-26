(ns amb.synth.audioloop
  (:use clojure.contrib.seq-utils)
  (:import [javax.sound.sampled 
	    AudioFormat AudioInputStream DataLine DataLine$Info SourceDataLine 
	    LineUnavailableException AudioSystem]))

;; -- utility
(defn audiochan-open [srate]
  "Opens an audio channel line according to parameters and returns it"
  (let [af   (AudioFormat. srate 8 1 true true)
        info (DataLine$Info. SourceDataLine af)
        #^SourceDataLine src  (AudioSystem/getLine info)]
    (.open src af) 
    (.start src) 
    src))

(defn audiochan-close [#^SourceDataLine chan]
  "Closes audio channel line"
  (.drain chan)
  (.stop chan) 
  (.close chan))

; -- output
(def bufsize (atom 512))
(def dl (atom nil))
(def running (atom false))

; audio-renderer is seen as a function that returns a value between -1.0 to 1.0
; for each of its invocation
(defn eng-start [audio-renderer]
  (reset! dl (audiochan-open 44000))
  (reset! running true)
  (def eng-thread 
       (Thread. (fn []
	(let [abuf (make-array Byte/TYPE @bufsize)]
	  (while (true? @running)
	    (dotimes [i @bufsize]
	      (aset #^bytes abuf (int i) (byte (* (audio-renderer) 127))))
	    (.write #^SourceDataLine @dl abuf 0 @bufsize))))))
  (.start eng-thread))

(defn eng-stop []
  (reset! running false)
  (.join eng-thread 2000) ; wait 2 secs at most then terminate
  (.interrupt eng-thread)
  (if (not (nil? @dl))
    (audiochan-close @dl)))

