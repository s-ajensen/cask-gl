(ns cask.lwjgl.system.memory-stack
  (:import (java.nio FloatBuffer)
           (org.joml Matrix4f)
           [org.lwjgl.system MemoryStack]))

(defn stackPush []
  (MemoryStack/stackPush))

(defn mallocInt [stack n]
  (.mallocInt stack n))

(defn mallocFloat [stack n]
  (.mallocFloat stack n))

(defn callocInt [stack size]
  (.callocInt stack size))

(defn callocFloat [stack size]
  (.callocFloat stack size))

(defn put [buf offset data]
  (.put buf offset data))

(defn store [^FloatBuffer buf ^Matrix4f value]
  (.get value buf))