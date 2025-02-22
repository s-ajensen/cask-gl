(ns cask.lwjgl.system.memory-util
  (:import [org.lwjgl.system MemoryUtil]))

(def NULL MemoryUtil/NULL)

(defn free [addr]
  (MemoryUtil/memFree addr))