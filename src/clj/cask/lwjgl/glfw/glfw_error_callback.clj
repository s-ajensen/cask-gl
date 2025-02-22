(ns cask.lwjgl.glfw.glfw-error-callback
  (:import [org.lwjgl.glfw GLFWErrorCallback]))

(defn createPrint [stream]
  (GLFWErrorCallback/createPrint stream))