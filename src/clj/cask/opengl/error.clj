(ns cask.opengl.error
  (:require [cask.lwjgl.opengl.gl20 :as gl]))

(def info-len 1024)

(defn shader-init-err []
  (Exception. "Could not initialize shader"))

(defn shader-create-err [type]
  (Exception. (str "Error creating shader of type: " type)))

(defn shader-compile-err [shader-id ]
  (Exception. (str "Error compiling shader:\n" (gl/getShaderInfoLog shader-id info-len))))

(defn shader-link-err [program-id]
  (Exception. (str "Error linking shader:\n" (gl/getProgramInfoLog program-id info-len))))