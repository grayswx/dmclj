(ns gl.sprite
  (:require [gl])
  (:use [state])
  (:import (org.lwjgl.opengl Display DisplayMode GL11)))

;; State stuff
(defn init-texture
  "Sets up the correct mode to use textures."
  []
  (GL11/glEnable GL11/GL_BLEND)
  (GL11/glEnable GL11/GL_TEXTURE_2D)    
  (GL11/glTexEnvi GL11/GL_TEXTURE_ENV GL11/GL_TEXTURE_ENV_MODE GL11/GL_REPLACE)
  (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)
  (GL11/glDisable GL11/GL_DEPTH_TEST))
(defn init-view
  "Initializes a 2D view."
  [width height]
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glLoadIdentity)
  (GL11/glOrtho 0 width 0 height 0 1)
  (GL11/glMatrixMode GL11/GL_MODELVIEW)
  (GL11/glLoadIdentity)
  (GL11/glTranslatef 0.375 0.375 0))	;So pixels are aligned.
(defn init
  "Sets GL to Sprite Mode."
  ([]
     (let [mode (Display/getDisplayMode)]
       (init (.getWidth mode) (.getHeight mode))))
  ([width height]
     (init-texture)
     (init-view width height)))

(defaction gl/state :sprite init)

;; Drawing functions.
(defn draw
  "Draws a texture onto the screen."
  ([[id w h] x y]
     (draw id w h x y))
  ([id w h x y]
     (set-state gl/state :sprite)
     (GL11/glBindTexture GL11/GL_TEXTURE_2D id)
     (GL11/glPushMatrix)
     (GL11/glTranslatef x y 0)
     (GL11/glBegin GL11/GL_QUADS)
     (GL11/glTexCoord2f 0 0)
     (GL11/glVertex3i 0 0 0)
     (GL11/glTexCoord2f 0 1)
     (GL11/glVertex3i 0 h 0)
     (GL11/glTexCoord2f 1 1)
     (GL11/glVertex3i w h 0)
     (GL11/glTexCoord2f 1 0)
     (GL11/glVertex3i w 0 0)
     (GL11/glEnd)
     (GL11/glPopMatrix)))

