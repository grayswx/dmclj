(ns resource
  (:refer-clojure :exclude (intern find))
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
(defref gl/state :on resources {}
  "[ref] A map of names to their corresponding resources.")

;; Locations
(defn add-location
  "Adds a location to search for resources in."
  [new-dir]
  (dosync
   (alter directories conj new-dir)))

(defn- list-files
  "Takes a printf style string and returns the result of
  applying the format string to a each of the values in
  any supplied collections.
  Ex: (list-files \"<%d%s>\" (range 3) ['a 'b 'c])
  Returns (\"<0a>\" \"<1b>\" \"<2c>\")."
  [string arglist]
  (apply map (fn [& arglist2]
	       (apply format string arglist2))
	 arglist))

(defn- #^File find-file
  "Finds the given file in one of the directories.
  Returns nil if no file can be found."
  [#^String fnm]
  (some #(let [#^File new-file (File. % fnm)]
	   (if (.isFile new-file) new-file))
	@directories))

;; Resource Types
(defn- find-type
  "Finds the type of a given file."
  [#^File f]
  (type-extensions
   (second 
    (re-find #".*\.(.*)" 
	     (.getName f)))))

(defmulti create
  "Creates a resource from a file."
  find-type)
(defmethod create :image
  [#^File file]
  #^:image (gl.tex/load-tex file))

;; Public functions.

(defn intern
  "Interns a resource.  Returns the given resource."
  [name rsrc]
  (dosync
   (alter resources assoc name rsrc))
  rsrc)

(defn intern-file
  "Loads, interns, and returns a resource from a file.
  Does so even if the resource has already been loaded."
  [name]
  (if-let [file (find-file name)]
    (let [rsrc (create file)]
      (intern name rsrc))
    (throw (new IOException 
		(str "File not found: " name)))))
    
(defn find
  "Finds an interned resource.
  Returns nil if resource does not exist."
  [name]
  (if-let [target (resources name)]
    target))

(defn grab
  "Grabs an interned resource, loading it if needed.
  The second form grabs all resources matching a given
  pattern."
  ([name]
     (if-let [target (resources name)]
       target
       (intern-file name)))
  ([format & args]
     (map grab (list-files format args))))

