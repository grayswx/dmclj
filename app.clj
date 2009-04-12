(ns app
  (:import (org.lwjgl.input Keyboard)
	   (org.lwjgl.opengl Display))
  (:require [gl]
	    [gl.sprite :as sprite]
	    [resource]
	    [clojure.contrib.test-is])
  (:gen-class))

(declare im-red0)

(defn start-fn [init]
  (resource/add-location "image")
  (gl/set-size 400 300)
  (gl/set-fullscreen false)
  (init)
  (def im-red0 (resource/grab "red0.png")))

(defn loop-fn []
  (gl/clear 0.7 0.9 0.34)
  (sprite/draw im-red0 0 0)
  (not (gl/quit?)))

(defn -main []
  (gl/run start-fn loop-fn))
