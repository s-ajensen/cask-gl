(ns cask.opengl.buf-obj
  (:require [cask.lwjgl.opengl.gl30 :as gl30]
            [cask.lwjgl.system.memory-stack :as mem-stack]))

(defmacro with-buffer [[buf val] & body]
  `(let [buf# ~buf val# ~val]
     (gl30/bindBuffer buf# val#)
     ~@body
     (gl30/bindBuffer buf# 0)))

; --- VBOs ---

(defn- load-buffer! [stack data buf-type alloc-fn]
  (let [vbo-id (gl30/genBuffers)
        buf (alloc-fn stack (count data))]
    (mem-stack/put buf 0 data)
    (with-buffer [buf-type vbo-id]
      (gl30/bufferData buf-type buf gl30/STATIC_DRAW))
    vbo-id))

(defrecord Vbo [id size])
(defrecord Ebo [id])

; may need to use doubles...
(defn load-vbo! [stack data]
  (load-buffer! stack data gl30/ARRAY_BUFFER mem-stack/callocFloat))

(defn load-ebo! [stack data]
  (load-buffer! stack data gl30/ELEMENT_ARRAY_BUFFER mem-stack/callocInt))

; --- VAOs ---

(defmacro with-vao [vao-id & body]
  `(let [vao-id# ~vao-id]
     (gl30/bindVertexArray vao-id#)
     ~@body
     (gl30/bindVertexArray 0)))

; there may be an optimization here for assigning many vbos at once
; without binding/unbinding vao each time
(defn assign-vbo! [vao-id {:keys [id size] :as _vbo} position]
  (with-vao vao-id
    (with-buffer [gl30/ARRAY_BUFFER id]
      (gl30/enableVertexAttribArray position)
      (gl30/vertexAttribPointer position size gl30/FLOAT false 0 0))))

(defn assign-ebo! [vao-id {:keys [id] :as _ebo}]
  (with-vao vao-id
    (gl30/bindBuffer gl30/ELEMENT_ARRAY_BUFFER id)))