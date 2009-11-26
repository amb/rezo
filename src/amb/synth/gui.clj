(ns amb.synth.gui
  (:use amb.synth.audioloop amb.synth.engine)
  (:import (javax.swing JFrame JPanel JButton JOptionPane JSlider JLabel
			JComboBox)
	   (javax.swing.event ChangeListener ChangeEvent 
			      ListSelectionListener ListSelectionEvent)
	   (java.awt.event ActionListener WindowListener
			   ItemEvent ItemListener)
	   (java.awt GridLayout)))

(defn lin-resp [x] x)
(defn log-resp [x] (Math/pow x 2.0))

(defmacro bind-slider [sld scale idx lbl resp]
  `(.addChangeListener ~sld
     (proxy [ChangeListener] []
       (stateChanged [evt#]
         (let [val# (* (~resp (/ (double (.. evt# getSource getValue)) 1000.0)) ~scale)]
	   (aset amb.synth.engine/constm ~idx val#)
	   (.setText ~lbl (format "%.5f" val#)))))))

(def car-vol-slider (JSlider. JSlider/HORIZONTAL 0 1000 1000))
(def car-frq-slider (JSlider. JSlider/HORIZONTAL 0 1000 200))

(def mod-vol-slider (JSlider. JSlider/HORIZONTAL 0 1000 100))
(def mod-frq-slider (JSlider. JSlider/HORIZONTAL 0 1000 100))

(def car-vol-label (JLabel. "0.0" JLabel/LEFT))
(def car-frq-label (JLabel. "0.0" JLabel/LEFT))
(def mod-vol-label (JLabel. "0.0" JLabel/LEFT))
(def mod-frq-label (JLabel. "0.0" JLabel/LEFT))

(bind-slider car-vol-slider 1.00 1 car-vol-label log-resp)
(bind-slider car-frq-slider 0.03 0 car-frq-label log-resp)
(bind-slider mod-vol-slider 4.0 3 mod-vol-label lin-resp)
(bind-slider mod-frq-slider 4.0 2 mod-frq-label lin-resp)

(defmacro bind-combob [cb osc]
  `(.addActionListener ~cb 
     (proxy [ActionListener] []
       (actionPerformed [evt#]
         (let [s# (.getSelectedIndex ~cb)]  
	     (cond (== s# 0) (reset! ~osc osc-type-sin)
		   (== s# 1) (reset! ~osc osc-type-saw)
		   (== s# 2) (reset! ~osc osc-type-sqr)))))))

(def car-wave (JComboBox. (into-array ["SIN" "SAW" "SQR"])))
(def mod-wave (JComboBox. (into-array ["SIN" "SAW" "SQR"])))

(bind-combob car-wave osc-2-func)
(bind-combob mod-wave osc-1-func)

(def frame (JFrame. "FM synth"))

; -- stop synth when window closes
(defn on-close [] (eng-stop))
(.addWindowListener frame 
  (proxy [WindowListener] []
    (windowClosing [arg0] (on-close))))

(.setLayout frame (GridLayout. 8 3 5 5))

(doto frame
  (.add (JLabel. "Carrier volume" JLabel/RIGHT))
  (.add car-vol-slider)
  (.add car-vol-label)

  (.add (JLabel. "Carrier frequency" JLabel/RIGHT))
  (.add car-frq-slider)
  (.add car-frq-label)

  (.add (JLabel. "Modifier volume" JLabel/RIGHT))
  (.add mod-vol-slider)
  (.add mod-vol-label)

  (.add (JLabel. "Modifier frequency" JLabel/RIGHT))
  (.add mod-frq-slider)
  (.add mod-frq-label)
  
  (.add (JLabel. "Carrier waveform" JLabel/RIGHT))
  (.add car-wave)
  (.add (JLabel. "" JLabel/LEFT))

  (.add (JLabel. "Modifier waveform" JLabel/RIGHT))
  (.add mod-wave)
  (.add (JLabel. "" JLabel/LEFT))

  (.add (JLabel. "MIDI input" JLabel/RIGHT))
  (.add (JComboBox. (into-array ["MIDI #1" "MIDI #2"])))
  (.add (JLabel. "" JLabel/LEFT)))

(doto frame
  (.setResizable false)
  (.setSize 400 400)
  (.pack)
  (.setVisible true))

(eng-start do-synth)
