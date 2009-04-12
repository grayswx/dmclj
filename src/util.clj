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

