(ns util
  (:import (java.io File)
	   (java.awt Image)
	   (org.apache.sanselan Sanselan))
  (:use [clojure.contrib.def]))



(defmulti ->String
  "Converts its argument to a string, if possible."
  class)
(defmethod ->String String [#^String s] s)
(defmethod ->String File [#^File f] (.getName f))


(defmulti ->File
  "Converts its argument to a file, if possible."
  class)
(defmethod ->File String [#^String s] (File. s))
(defmethod ->String File [#^File f] f)


(defmulti ->Image
  "Converts its argument to an image, if possible."
  class)
(defmethod ->Image String [s]
  (Sanselan/getBufferedImage (File. s)))
(defmethod ->Image File [f]
  (Sanselan/getBufferedImage f))
(defmethod ->Image Image [i] i)

(defn tag
  "Returns the tag on an object."
  [obj]
  (^obj :tag))

(defn pass-first
  "Passes a function the first argument, ignoring the rest."
  [fun]
  (fn [arg & ignore]
    (fun arg)))

(defn list-files
  "Takes a printf style string and returns the result of
  applying the format string to a each of the values in
  any supplied collections.
  Ex: (list-files \"<%d%s>\" (range 3) ['a 'b 'c])
  Returns (\"<0a>\" \"<1b>\" \"<2c>\")."
  [string arglist]
  (apply map (fn [& arglist2]
	       (apply format string arglist2))
	 arglist))

