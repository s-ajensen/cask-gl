(ns cask.glfw
  (:require [cask.lwjgl.glfw.glfw :as glfw]
            [cask.lwjgl.glfw.glfw-error-callback :as GLFWErrorCallback]
            [cask.lwjgl.system.memory-stack :as MemoryStack]
            [cask.lwjgl.system.memory-util :as MemoryUtil])
  (:import (org.lwjgl.glfw GLFW))
  )

(defn position-window [window]
  (with-open [stack (MemoryStack/stackPush)]
    (let [pWidth (.mallocInt stack 1)
          pHeight (.mallocInt stack 1)
          vidmode (glfw/getVideoMode (glfw/getPrimaryMonitor))]
      (GLFW/glfwGetFramebufferSize window pWidth pHeight)
      [(.get pWidth 0) (.get pHeight 0)]
      #_(glfw/setWindowPos window (/ (- (.width vidmode) (.get pWidth 0)) 2)
                         (/ (- (.height vidmode) (.get pHeight 0)) 2)))))

(defrecord Window [handle width height])

(defn init
  ([width height title]
   (let [error-callback (GLFWErrorCallback/createPrint System/out)]
     (.set error-callback)

     (when-not (glfw/init)
       (throw (IllegalStateException. "Unable to initialize GLFW")))

     (glfw/defaultWindowHints)

     (glfw/windowHint glfw/VISIBLE glfw/FALSE)
     (glfw/windowHint glfw/RESIZABLE glfw/TRUE)

     ; TODO - dispatch on detected OS
     (glfw/windowHint GLFW/GLFW_CONTEXT_VERSION_MAJOR 3)
     (glfw/windowHint GLFW/GLFW_CONTEXT_VERSION_MINOR 2)
     ;(glfw/windowHint GLFW/GLFW_OPENGL_PROFILE GLFW/GLFW_OPENGL_COMPAT_PROFILE)
     (glfw/windowHint GLFW/GLFW_OPENGL_PROFILE GLFW/GLFW_OPENGL_CORE_PROFILE)
     (glfw/windowHint GLFW/GLFW_OPENGL_FORWARD_COMPAT GLFW/GLFW_TRUE)

     (let [w (glfw/createWindow width height title MemoryUtil/NULL MemoryUtil/NULL)]
       (when (= 0 w)
         (throw (RuntimeException. "Failed to create the GLFW window")))

       (let [[width height] (position-window w)]
         (glfw/makeContextCurrent w)
         (glfw/swapInterval 0)
         (glfw/showWindow w)
         (->Window w width height)))))
  ([width height]
   (init width height "Cask"))
  ([]
   (init 300 300)))