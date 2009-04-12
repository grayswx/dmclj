(ns draw
  (:use [util]))

(defmulti draw
  "Draws an object to the screen."
  (pass-first tag))

  