(ns util
  (:import (java.io File))
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

(defn tag
  "Returns the tag on an object."
  [obj]
  (^obj :tag))

(defn pass-first
  "Passes a function the first argument, ignoring the rest."
  [fun]
  (fn [arg & ignore]
    (fun arg)))