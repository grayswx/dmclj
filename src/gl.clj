(ns gl
  (:import (org.lwjgl.opengl Display 
			     DisplayMode
			     GL11 
			     OpenGLException)
	   (org.lwjgl.input Keyboard))
  (:use [state]
	[clojure.contrib.def]))

;; State of the gl.
(defstate state :off)
(defaction state :off #(Display/destroy))
(defaction state :on #(Display/create))

;; Generic gl actions.
(defn clear
  "Clears the screen."
  ([] (GL11/glClear GL11/GL_COLOR_BUFFER_BIT))
  ([red blue green]
     (GL11/glClearColor red blue green 1.0)
     (GL11/glClear GL11/GL_COLOR_BUFFER_BIT))
  ([red blue green alpha]
     (GL11/glClearColor red blue green alpha)
     (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)))

;; GL Interface.
(defvar fps (ref 60)
  "The number of frames to advance per second.")

(defn size
  "Returns the size of the display."
  []
  (let [disp (Display/getDisplayMode)]
    [(.getWidth disp),
     (.getHeight disp)]))

(defn set-size
  "Sets the size of the display."
  [width height]
  (Display/setDisplayMode
   (DisplayMode. width height)))

(defn set-fullscreen
  "Sets whether or not fullscreen is on."
  [boolean]
  (Display/setFullscreen boolean))

(defn quit?
  "True if the user wants to quit.
  Escape is pressed or the window was closed."
  []
  (or (Keyboard/isKeyDown Keyboard/KEY_ESCAPE) 
      (Display/isCloseRequested)))
  
(defn run
  "Starts the main gl loop.
  Start should take a single argument.  This shall be a
  function of no arguments that initializes gl.  Do all
  pre-initalization code beforehand, then call it, then
  perform all post-initialization code.
  Loop-fn is called on every iteration.  If it returns
  false, the loop exits."
  [start loop-fn]
  (start #(set-state state :on))
  (loop []
    (Display/update)
    (if (loop-fn)
      (do (Display/sync @fps)
	  (recur))
      (set-state state :off))))