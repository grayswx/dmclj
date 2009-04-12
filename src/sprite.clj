(ns sprite
  (:require [gl.sprite]
	    [resource]
	    [animation])
  (:use [draw]))

(defstruct sprite :animation :frame :x :y)

(defn make
  "Creates a sprite."
  ([animation] (make animation 0 0))
  ([animation x y]
     (ref (struct sprite (resource/find animation) 0 x y)
	  :meta {:tag :sprite})))

(defn move
  "Moves a sprite."
  [sprite xoff yoff]
  (dosync 
   (let [ss @sprite
	 new-x (+ (ss :x) xoff)
	 new-y (+ (ss :y) yoff)]
   (alter sprite assoc :x new-x :y new-y))))

(defn set-location
  "Sets the location of a sprite."
  [sprite x y]
  (dosync
   (alter sprite assoc :x x :y y)))

(defn set-frame
  "Sets the frame of a sprite."
  [sprite frame]
  (dosync
   (alter sprite assoc :frame frame)))

(defn advance
  "Advances a sprite by one frame."
  [sprite]
  (dosync
   (let [ss @sprite
	 length (^(ss :animation) :length)
	 frame (ss :frame)]
     (alter sprite assoc :frame (mod (inc frame) length)))))

(defmethod draw/draw :sprite
  [sprite]
  (let [ss @sprite
	anim (ss :animation)
	anim-meta ^anim]
    (gl.sprite/draw 
     (nth anim (ss :frame))
     (anim-meta :width)
     (anim-meta :height)
     (ss :x)
     (ss :y))))