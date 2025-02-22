(ns cask.mesh-renderer
  (:require [c3kit.apron.corec :as ccc]
            [cask.lwjgl.opengl.gl30 :as gl30]
            [cask.camera :as camera]
            [cask.lwjgl.system.memory-stack :as mem-stack]
            [cask.opengl.shader :as shader]
            [cask.opengl.buf-obj :as bo]
            [cask.vbo :as vbo-old])
  (:import (org.joml Matrix4f)
           (org.lwjgl.opengl GL30)))

(defn model-view-matrix [{:keys [pos rot scale]} {:keys [transform] :as cam}]
  (let [mvm (-> (Matrix4f.)
                .identity
                (.translate pos)
                (.rotateX (float (Math/toRadians (- (.x rot)))))
                (.rotateY (float (Math/toRadians (- (.y rot)))))
                (.rotateZ (float (Math/toRadians (- (.z rot)))))
                (.scale (float scale)))
        view-mat (Matrix4f. (camera/->view-matrix transform))]
    (.mul view-mat mvm)))

(defprotocol MeshRenderer
  (render [_ shader]))

(defrecord TextureRenderer [vao mesh texture]
  MeshRenderer
  (render [_ shader]
    (gl30/bindVertexArray vao)
    (shader/bind (:program-id shader))
    (run! shader/set-uniform (:uniforms shader))

    (GL30/glActiveTexture GL30/GL_TEXTURE0)
    (GL30/glBindTexture GL30/GL_TEXTURE_2D (:id texture))

    (gl30/bindVertexArray vao)
    (gl30/drawElements gl30/TRIANGLES (:vertexCount mesh) gl30/UNSIGNED_INT 0)

    (gl30/bindVertexArray 0)

    (shader/unbind)))

(defn ->texture-renderer [mesh texture tex-coords]
  (with-open [stack (mem-stack/stackPush)]
    (let [vao-id (gl30/genVertexArrays)]
      (gl30/bindVertexArray vao-id)
      (run! vbo-old/bind (:vbos mesh))
      (bo/assign-vbo! vao-id (bo/->Vbo (bo/load-vbo! stack (float-array tex-coords)) 3) 1)
      (->TextureRenderer vao-id mesh texture))))

(defrecord ColorRenderer [vao mesh colors]
  MeshRenderer
  (render [_ shader]
    (gl30/bindVertexArray vao)
    (shader/bind (:program-id shader))
    (run! shader/set-uniform (:uniforms shader))

    (gl30/drawElements gl30/TRIANGLES (:vertexCount mesh) gl30/UNSIGNED_INT 0)

    (gl30/bindVertexArray 0)

    (shader/unbind)))

(defn ->color-renderer [mesh colors]
  (with-open [stack (mem-stack/stackPush)]
    (let [vao-id (gl30/genVertexArrays)]
      (gl30/bindVertexArray vao-id)
      (run! vbo-old/bind (:vbos mesh))
      (bo/assign-vbo! vao-id (bo/->Vbo (bo/load-vbo! stack (float-array colors)) 3) 1)
      (->ColorRenderer vao-id mesh colors))))

(defrecord NormalizedRenderer [vao mesh texture normals]
  MeshRenderer
  (render [_ shader]
    (gl30/bindVertexArray vao)
    (shader/bind (:program-id shader))
    (run! shader/set-uniform (:uniforms shader))

    (GL30/glActiveTexture GL30/GL_TEXTURE0)
    (GL30/glBindTexture GL30/GL_TEXTURE_2D (:id texture))

    (gl30/drawElements gl30/TRIANGLES (:vertexCount mesh) gl30/UNSIGNED_INT 0)

    (gl30/bindVertexArray 0)

    (shader/unbind)))

(defn ->normalized-renderer [mesh texture tex-coords normals]
  (with-open [stack (mem-stack/stackPush)]
    (let [vao-id (gl30/genVertexArrays)]
      (gl30/bindVertexArray vao-id)
      (run! vbo-old/bind (:vbos mesh))
      (bo/assign-vbo! vao-id (bo/->Vbo (bo/load-vbo! stack (float-array tex-coords)) 2) 1)
      (bo/assign-vbo! vao-id (bo/->Vbo (bo/load-vbo! stack (float-array normals)) 3) 2)
      (->TextureRenderer vao-id mesh texture))))