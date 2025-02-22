(ns cask.lwjgl.opengl.gl30
  (:import (org.lwjgl.opengl GL30)))

(def ARRAY_BUFFER GL30/GL_ARRAY_BUFFER)
(def STATIC_DRAW GL30/GL_STATIC_DRAW)
(def FLOAT GL30/GL_FLOAT)
(def UNSIGNED_INT GL30/GL_UNSIGNED_INT)
(def TRIANGLES GL30/GL_TRIANGLES)
(def ELEMENT_ARRAY_BUFFER GL30/GL_ELEMENT_ARRAY_BUFFER)
(def COMPILE_STATUS GL30/GL_COMPILE_STATUS)
(def LINK_STATUS GL30/GL_LINK_STATUS)
(def TEXTURE_2D GL30/GL_TEXTURE_2D)
(def TEXTURE_MIN_FILTER GL30/GL_TEXTURE_MIN_FILTER)
(def TEXTURE_MAG_FILTER GL30/GL_TEXTURE_MAG_FILTER)
(def LINEAR GL30/GL_LINEAR)
(def RGBA GL30/GL_RGBA)
(def UNSIGNED_BYTE GL30/GL_UNSIGNED_BYTE)

(defn genVertexArrays []
  (GL30/glGenVertexArrays))

(defn bindVertexArray [array]
  (GL30/glBindVertexArray array))

(defn deleteVertexArrays [array]
  (GL30/glDeleteVertexArrays array))

(defn bufferData [target data usage]
  (GL30/glBufferData target data usage))

(defn bindBuffer [target buffer]
  (GL30/glBindBuffer target buffer))

(defn genBuffers []
  (GL30/glGenBuffers))

(defn deleteBuffers [buffer]
  (GL30/glDeleteBuffers buffer))

(defn vertexAttribPointer [index size type normalized stride pointer]
  (GL30/glVertexAttribPointer index size type normalized stride pointer))

(defn enableVertexAttribArray [index]
  (GL30/glEnableVertexAttribArray index))

(defn disableVertexAttribArray [index]
  (GL30/glDisableVertexAttribArray index))

(defn drawArrays [mode first count]
  (GL30/glDrawArrays mode first count))

(defn drawElements [mode count type indices]
  (GL30/glDrawElements mode count type indices))

(defn uniform1i [location value]
  (GL30/glUniform1i location value))

(defn genTextures []
  (GL30/glGenTextures))

(defn bindTexture [target texture]
  (GL30/glBindTexture target texture))

(defn texParameteri [target pname param]
  (GL30/glTexParameteri target pname param))

(defn texImage2D [target level internal-format width height border format type pixels]
  (GL30/glTexImage2D target level internal-format width height border format type pixels))

(defn generateMipmap [target]
  (GL30/glGenerateMipmap target))