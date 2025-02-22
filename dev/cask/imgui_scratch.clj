(ns cask.imgui-scratch
  (:require [c3kit.apron.corec :as ccc]
            [c3kit.apron.time :as time]
            [cask.glfw :as window]
            [cask.lwjgl.glfw.glfw :as glfw]
            [cask.camera :as camera]
            [cask.core :as core]
            [cask.mesh-renderer :as renderer]
            [cask.transform :as transform]
            [cask.lwjgl.opengl.gl20 :as gl]
            [cask.mesh :as mesh]
            [cask.opengl.shader :as shader])
  (:import (cask.core GameEngine)
           (cask.mesh_renderer ColorRenderer)
           (org.joml Vector3f)
           [org.lwjgl.glfw Callbacks GLFW GLFWErrorCallback GLFWVidMode]
           [org.lwjgl.opengl GL GL11 GL20 GL30]
           [org.lwjgl.system MemoryStack MemoryUtil]
           [org.ice1000.jimgui JImGui]
           [org.ice1000.jimgui.util JniLoader]))

(def verts [-0.5, 0.5, 0.5,
            -0.5, -0.5, 0.5,
            0.5, -0.5, 0.5,
            0.5, 0.5, 0.5,
            -0.5, 0.5, -0.5,
            0.5, 0.5, -0.5,
            -0.5, -0.5, -0.5,
            0.5, -0.5, -0.5])

(def colors [0.5, 0.0, 0.0,
             0.0, 0.5, 0.0,
             0.0, 0.0, 0.5,
             0.0, 0.5, 0.5,
             0.5, 0.0, 0.0,
             0.0, 0.5, 0.0,
             0.0, 0.0, 0.5,
             0.0, 0.5, 0.5])

(def idxs [0, 1, 3, 3, 1, 2,
           4, 0, 3, 5, 4, 3,
           3, 2, 7, 5, 3, 7,
           6, 1, 0, 6, 0, 4,
           2, 1, 6, 2, 6, 7,
           7, 6, 4, 7, 4, 5])

(deftype ProjectionEngine [window]
  GameEngine
  (setup [this]
    (JniLoader/load)
    (let [proj-matrix (camera/->proj-matrix 60.0 1.0 0.01 1000.0)
          vert (shader/compile! (slurp "resources/shaders/vertex/projected.vert") gl/VERTEX_SHADER)
          frag (shader/compile! (slurp "resources/shaders/fragment/simple.frag") gl/FRAGMENT_SHADER)
          program (shader/->program [frag vert])
          mesh (mesh/->mesh verts idxs)
          imgui (JImGui/fromExistingPointer (:handle window))]
      {:proj-matrix    proj-matrix
       :program        program
       :cam            (camera/->camera (transform/->transform (Vector3f. 0 0 0) (Vector3f. 0 0 0) 1.0))
       :mesh           mesh
       :transform      (transform/->transform (Vector3f. 0 0 -3) (Vector3f. 0 0 0) 1)
       :color-renderer (renderer/->color-renderer mesh colors)
       :scale          1.0
       :tick           1
       :imgui imgui}))
  (nextState [this state]
    (let [{:keys [handle]} window
          {:keys [tick]} state]
      (if (GLFW/glfwWindowShouldClose handle)
        :halt
        (let [scale (inc (Math/sin (Math/toRadians (* 1.5 tick))))]
          (assoc state :scale scale
                 :tick (inc tick)
                 :cam (camera/->camera (transform/->transform (Vector3f. 0 (* 2 (dec scale)) 0) (Vector3f. (* 10 (dec scale)) 0 0) 1.0))
                 :transform (transform/->transform (Vector3f. 0 0 -3) (Vector3f. tick tick tick) (* 0.5 (inc scale))))))))
  (render [this {:keys [proj-matrix program cam mesh transform color-renderer imgui]}]
    (let [{:keys [handle width height] :as state} window]
      (GLFW/glfwPollEvents)

      (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
      (GL11/glViewport 0 0 width height)

      (let [uniforms {(shader/create-uniform program "projectionMatrix") proj-matrix
                      (shader/create-uniform program "modelViewMatrix") (renderer/model-view-matrix transform cam)}
            shader (shader/->shader program uniforms)]

        (when-not (.windowShouldClose imgui)
          (.initNewFrame imgui)
          (.text imgui "Hello, World!")
          (.render imgui))

        #_(.render (ColorRenderer. (:vao color-renderer) mesh colors) shader))

      (GLFW/glfwSwapBuffers handle))))

(defn loop-fn [{:keys [handle width height] :as window}]
  (GL/createCapabilities)
  (GL11/glEnable GL11/GL_DEPTH_TEST)
  (let [engine (ProjectionEngine. window)]
    (core/game-loop engine 10)))

(defn -main []
  (let [window (window/init)]
    (try
      (loop-fn window)
      (finally
        (Callbacks/glfwFreeCallbacks (:handle window))
        (GLFW/glfwDestroyWindow (:handle window))
        (GLFW/glfwTerminate)
        (.free (GLFW/glfwSetErrorCallback nil))))))