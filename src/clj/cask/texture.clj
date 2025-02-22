(ns cask.texture
  (:require [cask.lwjgl.system.memory-stack :as mem-stack])
  (:import (de.matthiasmann.twl.utils PNGDecoder PNGDecoder$Format)
           (java.io FileInputStream)
           (java.nio ByteBuffer)
           (org.lwjgl.opengl GL30)
           (org.lwjgl.stb STBImage)))

(defn load [path]
  (with-open [stack (mem-stack/stackPush)]
    (let [width (.mallocInt stack 1)
          height (.mallocInt stack 1)
          channels (.mallocInt stack 1)
          buf (STBImage/stbi_load path width height channels 4)
          tex-id (GL30/glGenTextures)]
      (GL30/glBindTexture GL30/GL_TEXTURE_2D tex-id)
      (GL30/glPixelStorei GL30/GL_UNPACK_ALIGNMENT 1)
      (GL30/glTexParameteri GL30/GL_TEXTURE_2D GL30/GL_TEXTURE_MIN_FILTER GL30/GL_NEAREST)
      (GL30/glTexParameteri GL30/GL_TEXTURE_2D GL30/GL_TEXTURE_MAG_FILTER GL30/GL_NEAREST)
      (GL30/glTexImage2D GL30/GL_TEXTURE_2D 0 GL30/GL_RGBA (.get width) (.get height)
                         0 GL30/GL_RGBA GL30/GL_UNSIGNED_BYTE
                         buf)
      (GL30/glGenerateMipmap GL30/GL_TEXTURE_2D)
      [tex-id buf])))

(defrecord Texture [id buffer])

(defn ->texture [path]
  (apply ->Texture (load path)))