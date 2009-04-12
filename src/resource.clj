(ns resource
  (:refer-clojure :exclude (intern))
  (:import (java.io File IOException))
  (:require [gl]
	    [gl.tex])
  (:use [state]
	[util]
	[clojure.contrib.def]
	[clojure.contrib.test-is]))

;; Variables
(defvar- directories (ref #{""})
  "A list of all directories to check for resources in.")
(defvar- type-extensions
  {"bmp" :image,
   "png" :image,
   "gif" :image,}
  "A map of extensions to their corresponding type.")
(defvar- group-types
  {:image :animation}
  "A map of single types to their group types.")
(defref gl/state :on resources {})

;; Locations
(defn add-location
  "Adds a location to find resources in."
  [new-dir]
  (dosync
   (alter directories conj new-dir)))

;; Resource Types

(defn- find-type
  "Finds the type of a given string."
  [#^File f]
  (type-extensions
   (second 
    (re-find #".*\.(.*)" 
	     (.getName f)))))
(defn- find-group-type
  "Finds the group type of a given string."
  [#^File f]
  (group-types (find-type f)))

(defmulti create
  "Creates a resource from a file."
  find-type)
(defmethod create :image
  [#^File file]
  #^:image (gl.tex/load-tex file))

;; Resources

(defn- #^File find-file
  "Finds the given file in one of the directories.
  Returns nil if no file can be found."
  [#^String fnm]
  (some #(let [#^File new-file (File. % fnm)]
	   (if (.isFile new-file) new-file))
	@directories))

;; Grouped Resources
(defn- parse-multi-string
  "Takes a printf style string and returns the result of
  applying the format string to a each of the values in
  any supplied collections.
  Ex: (parse-multi-string \"<%d%s>\" (range 3) ['a 'b 'c])
  Returns (\"<0a>\" \"<1b>\" \"<2c>\")."
  [string arglist]
  (apply map (fn [& arglist2]
	       (apply format string arglist2))
	 arglist))
(declare grab)
(defn- grab-each
  "Grabs each of the possible resource names."
  [string arglist]
  (map grab (parse-multi-string string arglist)))
		 
(defmulti multicreate
  "Creates a resource collection."
  (fn [string & rest] (find-group-type string)))
(defmethod multicreate :animation
  [string arglist]
  (let [images (grab-each arglist)
	init-image (first images)
	width (nth init-image 1)
	height (nth init-image 2)]
    (with-meta
     (into [] (map first images))
     {:tag :animation
      :width width
      :height height
      :length (count images)})))    

(defn intern
  "Interns a resource."
  [name rsrc]
  (dosync
   (alter resources assoc name rsrc)))

(defn grab
  "Grab an interned resource, or load and intern if needed."
  ([format & args]
     (if-let [target (resources format)]
       target
       (let [rsrc (multicreate format args)]
	 (intern format rsrc)
	 rsrc)))
  ([name]
     (if-let [target (resources name)]
       target
       (if-let [file (find-file name)]
	 (let [rsrc (create file)]
	   (intern name rsrc)
	   rsrc)
	 (throw (new IOException 
		     (format "File not found: %s" name)))))))

   
