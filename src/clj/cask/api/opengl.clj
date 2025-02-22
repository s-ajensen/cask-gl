(ns cask.api.opengl
  (:require [cask.api.core :as cask]
            [cask.lwjgl.opengl.gl20 :as gl]
            [cask.lwjgl.opengl.gl30 :as gl30]
            [cask.lwjgl.system.memory-stack :as mem-stack]
            [cask.opengl.buf-obj :as bo]
            [cask.opengl.error :as err])
  (:import (java.nio ByteBuffer)
           (org.joml Matrix4f)))

; --- meshes ---
(defrecord GlMesh [vbo ebo size])
(defn- load-mesh! [verts idxs]
  (with-open [stack (mem-stack/stackPush)]
    (->GlMesh (bo/load-vbo! stack verts)
              (bo/load-ebo! stack idxs)
              (count idxs))))
; ^^^ meshes ^^^
; --- shaders ---
(defmacro with-attached-shaders [program-id shaders & body]
  `(let [program-id# ~program-id
         shaders# ~shaders]
     (run! (partial gl/attachShader program-id#) shaders#)
     ~@body
     (run! (partial gl/detachShader program-id#) shaders#)))

(defn- compile-shader [shader-code type]
  (let [shader-id (gl/createShader type)]
    (when (zero? shader-id)
      (throw (err/shader-create-err type)))
    (gl/shaderSource shader-id shader-code)
    (gl/compileShader shader-id)
    (when (zero? (gl/getShaderi shader-id gl/COMPILE_STATUS))
      (throw (err/shader-compile-err shader-id)))
    shader-id))

(defn- link-shaders [shaders]
  (let [program-id (gl/createProgram)]
    (when (= 0 program-id)
      (throw (err/shader-init-err)))
    (with-attached-shaders program-id shaders
      (gl/linkProgram program-id)
      (when (zero? (gl/getProgrami program-id gl/LINK_STATUS))
        (throw (err/shader-link-err program-id))))
    program-id))

(defn- get-uniform [shader name]
  (let [uniform (gl/getUniformLocation shader name)]
    (when (neg? uniform)
      (throw (Exception. (format "No such uniform '%s'" name))))
    uniform))

(defmulti -set-uniform (fn [_ value] (type value)))
(defmethod -set-uniform Matrix4f [location value]
  (with-open [stack (mem-stack/stackPush)]
    (let [buf (mem-stack/mallocFloat stack 16)]
      (.get value buf)
      (gl/uniformMatrix4fv location false buf))))

(defmethod -set-uniform Long [location value]
  (gl30/uniform1i location value))

; ^^^ shaders ^^^
; --- textures ---

(defrecord GlTexture [tex-id width height])

(defn- load-texture! [width height ^bytes data]
  (let [tex-id (gl30/genTextures)
        buffer (ByteBuffer/wrap data)]
    (gl30/bindTexture gl30/TEXTURE_2D tex-id)
    (gl30/texParameteri gl30/TEXTURE_2D gl30/TEXTURE_MIN_FILTER gl30/LINEAR)
    (gl30/texParameteri gl30/TEXTURE_2D gl30/TEXTURE_MAG_FILTER gl30/LINEAR)
    (gl30/texImage2D gl30/TEXTURE_2D 0 gl30/RGBA width height 0 gl30/RGBA gl30/UNSIGNED_BYTE buffer)
    (gl30/generateMipmap gl30/TEXTURE_2D)
    (->GlTexture tex-id width height)))

; ^^^ textures ^^^

(deftype GlRenderer []
  cask/Renderer
  (-load-mesh! [_ verts idxs] (load-mesh! verts idxs))

  (-compile-shader! [_ shader-code type] (compile-shader shader-code type))
  (-link-shaders! [_ shaders] (link-shaders shaders))
  (-get-uniform [_ shader name] (get-uniform shader name))
  (-set-uniform [_ location value] (-set-uniform location value))

  (-load-texture! [_ width height data]
    (load-texture! width height data)))