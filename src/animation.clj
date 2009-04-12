(ns animation
  (:require [resource :as rsrc])
  )

(defn make
  "Grabs an animation, loading it if necessary."
  [name format & args]
  (if-let [target (rsrc/find name)]
    target
    (let [img-list (apply rsrc/grab format args)
	  img1 (first img-list)]
      (rsrc/intern 
       name
       (with-meta (into [] (map first img-list))
		  {:tag :animation
		   :length (count img-list)
		   :width (nth img1 1)
		   :height (nth img1 2)})))))

(defn frame
  "Grabs the specified fram of an animation."
  [name frame]
  (nth (rsrc/find name) frame))
