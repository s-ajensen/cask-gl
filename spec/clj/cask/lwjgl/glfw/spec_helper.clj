(ns cask.lwjgl.glfw.spec-helper
  (:require [cask.lwjgl.glfw.glfw :as glfw]
            [speclj.core :refer [redefs-around stub]])
  (:import (org.lwjgl.glfw GLFWErrorCallback)))

(defn stub-glfw []
  (redefs-around [glfw/init (stub :glfwInit {:return true})
                  glfw/defaultWindowHints (stub :glfwDefaultWindowHints)
                  glfw/windowHint (stub :glfwWindowHint)
                  glfw/createWindow (stub :glfwCreateWindow {:return 1})
                  glfw/getVideoMode (stub :glfwGetVideoMode)
                  glfw/getPrimaryMonitor (stub :glfwGetPrimaryMonitor)
                  glfw/getWindowSize (stub :glfwGetWindowSize)
                  glfw/setWindowPos (stub :glfwSetWindowPos)
                  glfw/makeContextCurrent (stub :glfwMakeContextCurrent)
                  glfw/swapInterval (stub :glfwSwapInterval)
                  glfw/showWindow (stub :glfwShowWindow)]))
