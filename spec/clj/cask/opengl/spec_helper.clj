(ns cask.opengl.spec-helper
  (:require [cask.lwjgl.opengl.gl20 :as gl]
            [cask.lwjgl.opengl.gl30 :as gl30]
            [speclj.core :refer [redefs-around stub]]))

(def uniforms (atom {}))

(defn- set-uniform!
  ([location value]
   (swap! uniforms assoc location value))
  ([location _ value]
   (swap! uniforms assoc location value)))

(defn stub-gl20 []
  (redefs-around
    [gl/createProgram (stub :createProgram {:return :shader-id})
     gl/createShader (stub :createShader {:return 1})
     gl/shaderSource (stub :shaderSource)
     gl/compileShader (stub :compileShader)
     gl/getShaderi (stub :getShaderi {:return 1})
     gl/getShaderInfoLog (stub :getShaderInfoLog {:return ""})
     gl/attachShader (stub :attachShader)
     gl/linkProgram (stub :linkProgram)
     gl/getProgrami (stub :getProgrami {:return 1})
     gl/getProgramInfoLog (stub :getProgramInfoLog {:return ""})
     gl/detachShader (stub :detachShader)
     gl/validateProgram (stub :validateProgram)
     gl/useProgram (stub :useProgram)
     gl/deleteProgram (stub :deleteProgram)
     gl/getUniformLocation (stub :getUniformLocation {:return 1})
     gl/uniformMatrix4fv (stub :uniformMatrix4fv {:invoke set-uniform!})]))

(def cur-vao-id (atom 1))
(def cur-buf-id (atom 1))
(def vao (atom 0))
(def vaos (atom {}))
(def buffers (atom {}))
(def buffer-data (atom {}))
(def textures (atom {}))
(def cur-texture-id (atom 0))

(defn- bind-buff! [buffer id]
  (swap! buffers assoc buffer id)
  (when (= gl30/ELEMENT_ARRAY_BUFFER buffer)
    (swap! vaos update-in [@vao :vbos] conj id)))
(defn- buffer-data! [buffer data usage]
  (let [buf (get @buffers buffer)]
    (swap! buffer-data assoc buf {:data data :usage usage})))
(defn- gen-vao! []
  (let [id (keyword (str "vao-" @cur-vao-id))]
    (swap! cur-vao-id inc)
    id))
(defn- bind-vao! [vao-id]
  (reset! vao vao-id))
(defn- gen-buf! []
  (let [id (keyword (str "buf-" @cur-buf-id))]
    (swap! cur-buf-id inc)
    id))
(defn- enable-vao-idx! [idx]
  (swap! vaos update-in [@vao :vbos (get @buffers gl30/ARRAY_BUFFER)] merge
         {:enabled true}))
(defn- bind-vbo! [idx size type normalized stride offset]
  (swap! vaos update-in [@vao :vbos (get @buffers gl30/ARRAY_BUFFER)] merge
         {:idx idx :size size :type type :normalized normalized :stride stride :offset offset}))

(defn- gen-texture! []
  (swap! cur-texture-id inc))

(defn- bind-texture! [target texture]
  (swap! textures assoc :bound {:target target :id texture}))

(defn- tex-parameter! [target pname param]
  (swap! textures update-in [:parameters target] assoc pname param))

(defn- tex-image! [target level internalformat width height border format type pixels]
  (swap! textures assoc :image-data
         {:target target :level level :internalformat internalformat
          :width width :height height :border border :format format
          :type type :pixels pixels}))

(defn- generate-mipmap! [target]
  (swap! textures assoc :mipmaps-generated target))

(defn clear! []
  (reset! cur-vao-id 1)
  (reset! cur-buf-id 1)
  (reset! vao 0)
  (reset! vaos {})
  (reset! buffers {})
  (reset! buffer-data {})
  (reset! cur-texture-id 0)
  (reset! textures {}))

(defn stub-gl30 []
  (redefs-around
    [gl30/genVertexArrays (stub :genVertexArrays {:invoke gen-vao!})
     gl30/bindVertexArray (stub :bindVertexArray {:invoke bind-vao!})
     gl30/deleteVertexArrays (stub :deleteVertexArrays)
     gl30/bufferData (stub :bufferData {:invoke buffer-data!})
     gl30/bindBuffer (stub :bindBuffer {:invoke bind-buff!})
     gl30/genBuffers (stub :genBuffers {:invoke gen-buf!})
     gl30/deleteBuffers (stub :deleteBuffers)
     gl30/vertexAttribPointer (stub :vertexAttribPointer {:invoke bind-vbo!})
     gl30/enableVertexAttribArray (stub :enableVertexAttribArray {:invoke enable-vao-idx!})
     gl30/disableVertexAttribArray (stub :disableVertexAttribArray)
     gl30/uniform1i (stub :uniform1i {:invoke set-uniform!})
     gl30/genTextures (stub :genTextures {:invoke gen-texture!})
     gl30/bindTexture (stub :bindTexture {:invoke bind-texture!})
     gl30/texParameteri (stub :texParameteri {:invoke tex-parameter!})
     gl30/texImage2D (stub :texImage2D {:invoke tex-image!})
     gl30/generateMipmap (stub :generateMipmap {:invoke generate-mipmap!})]))
