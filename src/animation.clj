(ns animation
  (:require [resource :as rsrc]
	    [gl.tex :as tex])
  )

(defn make
  "Grabs an animation, loading it if necessary."
  [name format & args]
  (if-let [target (rsrc/find name)]
    target
    (let [tex-list (apply tex/grab format args)
	  tex1 (first tex-list)]
      (rsrc/intern 
       name
       (with-meta (into [] (map first tex-list))
		  {:tag :animation
		   :length (count tex-list)
		   :width (nth tex1 1)
		   :height (nth tex1 2)})))))

(defn frame
  "Grabs the specified frame of an animation."
  [name frame]
  (nth (rsrc/find name) frame))
