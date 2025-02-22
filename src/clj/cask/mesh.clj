(ns cask.mesh
  (:require [c3kit.apron.corec :as ccc]
            [cask.lwjgl.opengl.gl30 :as gl30]
            [cask.lwjgl.system.memory-stack :as mem-stack]
            [cask.opengl.buf-obj :as bo]
            [cask.vbo :as vbo-old]))

(defrecord Mesh [verts vertexCount vbos])

(defn ->mesh
  [verts idxs]
  (with-open [stack (mem-stack/stackPush)]
    (let [vert-vbo (bo/load-vbo! stack (float-array verts))
          idx-bvo (bo/load-ebo! stack (int-array idxs))]

      (gl30/bindBuffer gl30/ARRAY_BUFFER 0)

      (->Mesh vert-vbo (count idxs) [(vbo-old/->VertVbo vert-vbo 0) (vbo-old/->IdxVbo idx-bvo)]))))

(defn clean-up [mesh]
  (gl30/disableVertexAttribArray 0)
  (gl30/bindBuffer gl30/ARRAY_BUFFER 0)
  (gl30/deleteBuffers (:vboId mesh))
  (gl30/bindVertexArray 0)
  (gl30/deleteVertexArrays (:vaoId mesh)))