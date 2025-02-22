(ns cask.api.core
  (:require [c3kit.apron.app :as app]))

(defonce render-impl (app/resolution! :renderer/impl))

(defprotocol Renderer
  (-load-mesh! [this verts idxs])

  (-compile-shader! [this shader-code type])
  (-link-shaders! [this shaders])
  (-get-uniform [this program name])
  (-set-uniform [this location value])

  (-load-texture! [this width height data]))

(defn load-mesh!
  ([renderer verts idxs]
   (load-mesh! @renderer verts idxs))
  ([verts idxs]
   (-load-mesh! @render-impl verts idxs)))

(defn compile-shader!
  ([renderer shader-code type]
   (-compile-shader! @renderer shader-code type))
  ([shader-code type]
   (-compile-shader! @render-impl shader-code type)))

(defn link-shaders!
  ([renderer shaders]
   (-link-shaders! @renderer shaders))
  ([shaders]
   (-link-shaders! @render-impl shaders)))

(defn get-uniform
  ([renderer shader name]
   (-get-uniform renderer shader name))
  ([shader name]
   (-get-uniform @render-impl shader name)))

(defn set-uniform!
  ([renderer location value]
   (-set-uniform renderer location value))
  ([location value]
   (-set-uniform @render-impl location value)))

(defn load-texture!
  ([renderer width height data]
   (-load-texture! renderer width height data))
  ([width height data]
   (-load-texture! @render-impl width height data)))