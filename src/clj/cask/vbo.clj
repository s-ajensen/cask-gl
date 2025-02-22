(ns cask.vbo
  (:require [cask.lwjgl.opengl.gl30 :as gl30]
            [cask.lwjgl.system.memory-stack :as mem-stack]))

(defprotocol Vbo
  (bind [_]))
(defn- bind-buf [id offset len]
  (gl30/bindBuffer gl30/ARRAY_BUFFER id)
  (gl30/enableVertexAttribArray offset)
  (gl30/vertexAttribPointer offset len gl30/FLOAT false 0 0)
  (gl30/bindBuffer gl30/ARRAY_BUFFER 0))

(defrecord VertVbo [id offset]
  Vbo
  (bind [_] (bind-buf id offset 3)))

(defrecord IdxVbo [id]
  Vbo
  (bind [_] (gl30/bindBuffer gl30/ELEMENT_ARRAY_BUFFER id)))
