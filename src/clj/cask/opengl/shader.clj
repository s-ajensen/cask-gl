(ns cask.opengl.shader
  (:require [cask.lwjgl.opengl.gl30 :as gl30]
            [cask.opengl.error :as err]
            [cask.lwjgl.system.memory-stack :as mem-stack]
            [cask.lwjgl.opengl.gl20 :as gl])
  (:import (org.joml Matrix4f)
           (org.lwjgl.opengl GL30)))

(defn compile! [shader-code type]
  (let [shader-id (gl/createShader type)]
    (when (zero? shader-id)
      (throw (err/shader-create-err type)))
    (gl/shaderSource shader-id shader-code)
    (gl/compileShader shader-id)
    (when (zero? (gl/getShaderi shader-id gl/COMPILE_STATUS))
      (throw (err/shader-compile-err shader-id)))
    shader-id))

(defmacro with-attached-shaders [program-id shaders & body]
  `(let [program-id# ~program-id
        shaders# ~shaders]
     (run! (partial gl/attachShader program-id#) shaders#)
     ~@body
     (run! (partial gl/detachShader program-id#) shaders#)))

(defn bind [shader]
  (gl/useProgram shader))
(defn unbind []
  (gl/useProgram 0))

(defn link! [program-id shaders]
  (with-attached-shaders program-id shaders
    (gl/linkProgram program-id)
    (when (zero? (gl/getProgrami program-id gl/LINK_STATUS))
      (throw (err/shader-link-err program-id)))))

(defn ->program [shaders]
  (let [program-id (gl/createProgram)]
    (when (zero? program-id)
      (throw (err/shader-init-err)))
    (link! program-id shaders)
    program-id))

(defrecord Shader [program-id uniforms])

(defn ->shader [program-id uniforms]
  (->Shader program-id uniforms))

(defn create-uniform [program name]
  (let [location (gl/getUniformLocation program name)]
    (when (neg? location)
      (throw (Exception. (str "Could not find uniform " name))))
    location))

(defmulti -set-uniform (fn [_ value] (type value)))
(defmethod -set-uniform Matrix4f [location value]
  (with-open [stack (mem-stack/stackPush)]
    (let [buf (.mallocFloat stack 16)]
      (.get value buf)
      (gl/uniformMatrix4fv location false buf))))
(defmethod -set-uniform :default [location value]
  (GL30/glUniform1i location value))

(defn set-uniform
  ([[location value]]
   (set-uniform location value))
  ([location value]
   (-set-uniform location value)))