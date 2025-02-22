(ns cask.lwjgl.glfw.glfw
  (:import (java.nio IntBuffer)
           [org.lwjgl.glfw GLFW GLFWVidMode]))

(def VISIBLE GLFW/GLFW_VISIBLE)
(def RESIZABLE GLFW/GLFW_RESIZABLE)
(def TRUE GLFW/GLFW_TRUE)
(def FALSE GLFW/GLFW_FALSE)

(defn ^boolean init []
  (GLFW/glfwInit))

(defn defaultWindowHints []
  (GLFW/glfwDefaultWindowHints))

(defn windowHint [hint value]
  (GLFW/glfwWindowHint hint value))

(defn createWindow [width height title monitor share]
  (GLFW/glfwCreateWindow (int width) (int height) ^String title ^long monitor ^long share))

(defn ^GLFWVidMode getVideoMode [^long monitor]
  (GLFW/glfwGetVideoMode monitor))

(defn ^long getPrimaryMonitor []
  (GLFW/glfwGetPrimaryMonitor))

(defn getWindowSize [^long window ^IntBuffer pWidth ^IntBuffer pHeight]
  (GLFW/glfwGetWindowSize window pWidth pHeight))

(defn setWindowPos [^long window ^long xpos ^long ypos]
  (GLFW/glfwSetWindowPos window xpos ypos))

(defn makeContextCurrent [window]
  (GLFW/glfwMakeContextCurrent window))

(defn swapInterval [interval]
  (GLFW/glfwSwapInterval interval))

(defn showWindow [window]
  (GLFW/glfwShowWindow window))

(defn getTime []
  (GLFW/glfwGetTime))