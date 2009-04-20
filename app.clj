(ns app
  (:import (org.lwjgl.input Keyboard)
	   (org.lwjgl.opengl Display))
  (:require [gl]
	    [gl.tex :as tex]
	    [resource]
	    [animation]
	    [draw]
	    [sprite]
	    [clojure.contrib.test-is])
  (:gen-class))

(declare red0)

(defn start-fn [init]
  (resource/add-location "image")
  (gl/set-size 400 300)
  (gl/set-fullscreen false)
  (init)
  (animation/make :red0 "red0-%d.png" (range 4))
  (def red0 (sprite/make :red0 (- 200 16) (- 150 16))))

(defn loop-fn []
  (gl/clear 0.7 0.9 0.34)
  (draw/draw red0)
  (sprite/advance red0)
  (if (= (red0 :frame) 0)
    (sprite/set-frame red0 2))
  (sprite/move red0 -1 0)
  (if (<= (red0 :x) -32)
    (sprite/move red0 432 0))
  (not (gl/quit?)))

(defn -main []
  (doto (Thread. #(gl/run start-fn loop-fn))
    (.start)))
		 
