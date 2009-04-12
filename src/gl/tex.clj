(ns gl.tex
  (:import (java.awt AlphaComposite Color Graphics2D Image Rectangle)
	   (java.awt.color ColorSpace)
	   (java.awt.image BufferedImage ColorModel ComponentColorModel DataBuffer Raster)
	   (java.io File)
	   (java.nio ByteBuffer ByteOrder IntBuffer)
	   (javax.imageio ImageIO)
	   (org.lwjgl.opengl GL11 OpenGLException)
	   (org.apache.sanselan Sanselan))
  (:use util
	[clojure.contrib.def]))

(defvar- color-model
  (new ComponentColorModel
       (ColorSpace/getInstance ColorSpace/CS_sRGB)
       true
       false
       ComponentColorModel/TRANSLUCENT
       DataBuffer/TYPE_BYTE))

(defn- next-power-of-2
  "Returns the next highest power of 2."
  ([num] (next-power-of-2 num 1))
  ([num pow]
     (if (>= pow num) pow
	 (recur num (* 2 pow)))))

(defn- new-tex-id
  "Get a new texture id from gl."
  []
  (let [buffer (.asIntBuffer (doto (ByteBuffer/allocateDirect 4)
			       (.order (ByteOrder/nativeOrder))))]
    (try
     (GL11/glGenTextures buffer)
     (. buffer (get 0))
     (catch NullPointerException e
       (throw (new OpenGLException "GL is not initialized - cannot create texture id." e))))))

(defn gl-compatible-image
  "Generates an image which is compatible with gl."
  ([width height]
     (let [w (next-power-of-2 width)
	   h (next-power-of-2 height)]
       (let [image (new BufferedImage
			color-model
			(Raster/createInterleavedRaster DataBuffer/TYPE_BYTE w h 4 nil)
			false
			nil)]
	 (doto (.getGraphics image)
	   (.setComposite (AlphaComposite/getInstance AlphaComposite/CLEAR 0))
	   (.fill (new Rectangle w h))
	   (.dispose))
	 image)))
  ([image]
     (let [im (gl-compatible-image (.getHeight image nil) (.getWidth image nil))]
       (doto (.getGraphics im)
	 (.setComposite AlphaComposite/Src)
	 (.drawImage image 0 0 nil)
	 (.dispose))
       im)))

(defn extract-buffer
  #^{:doc "Extracts the byte data from a gl compatible image"}
  [image]
  (let [data (.getData (.getDataBuffer (.getRaster image)))]
    (doto (ByteBuffer/allocateDirect (alength data))
      (.order (ByteOrder/nativeOrder))
      (.put data 0 (alength data))
      (.flip))))

(defn make-texture
  "Makes a texture from an image."
  [image]
  (let [id (new-tex-id)
	im (gl-compatible-image image)]
    (GL11/glBindTexture GL11/GL_TEXTURE_2D id)
    (GL11/glTexImage2D GL11/GL_TEXTURE_2D
		       0
		       GL11/GL_RGBA
		       (.getWidth im)
		       (.getHeight im)
		       0
		       GL11/GL_RGBA
		       GL11/GL_UNSIGNED_BYTE
		       (extract-buffer im))
    (GL11/glTexParameteri GL11/GL_TEXTURE_2D
			  GL11/GL_TEXTURE_MIN_FILTER
			  GL11/GL_NEAREST)
    (GL11/glTexParameteri GL11/GL_TEXTURE_2D
			  GL11/GL_TEXTURE_MAG_FILTER
			  GL11/GL_NEAREST)
    [id (.getWidth im) (.getHeight im)]))

;; Loads a texture.
(defmulti load-tex #(class %))
(defmethod load-tex String [s]
  (load-tex (new File s)))
(defmethod load-tex File [f]
  (load-tex (Sanselan/getBufferedImage f)))
(defmethod load-tex BufferedImage [img]
  (make-texture img))


