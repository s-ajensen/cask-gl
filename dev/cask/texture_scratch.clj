(ns cask.texture-scratch
  (:require [c3kit.apron.corec :as ccc]
            [c3kit.apron.time :as time]
            [cask.glfw :as window]
            [cask.lwjgl.glfw.glfw :as glfw]
            [cask.texture :as texture]
            [cask.camera :as camera]
            [cask.core :as core]
            [cask.transform :as transform]
            [cask.mesh-renderer :as renderer]
            [cask.lwjgl.opengl.gl20 :as gl]
            [cask.mesh :as mesh]
            [cask.opengl.shader :as shader])
  (:import (cask.core GameEngine)
           (cask.mesh_renderer TextureRenderer)
           (org.joml Vector3f)
           [org.lwjgl.glfw Callbacks GLFW GLFWErrorCallback GLFWVidMode]
           [org.lwjgl.opengl GL GL11 GL20 GL30]
           [org.lwjgl.system MemoryStack MemoryUtil]))

(def verts [-0.5, 0.5, 0.5,
            -0.5, -0.5, 0.5,
            0.5, -0.5, 0.5,
            0.5, 0.5, 0.5,
            -0.5, 0.5, -0.5,
            0.5, 0.5, -0.5,
            -0.5, -0.5, -0.5,
            0.5, -0.5, -0.5,

            -0.5, 0.5, -0.5,
            0.5, 0.5, -0.5,
            -0.5, 0.5, 0.5,
            0.5, 0.5, 0.5,

            0.5, 0.5, 0.5,
            0.5, -0.5, 0.5,

            -0.5, 0.5, 0.5,
            -0.5, -0.5, 0.5,

            -0.5, -0.5, -0.5,
            0.5, -0.5, -0.5,
            -0.5, -0.5, 0.5,
            0.5, -0.5, 0.5,])

(def tex-coords [0.0, 0.0,
                 0.0, 1.0,
                 1.0 ,1.0 ,
                 1.0, 0.0,

                 0.0, 0.0,
                 1.0, 0.0,
                 0.0, 1.0,
                 1.0, 1.0,

                 0.0, 1.0,
                 1.0, 1.0,
                 0.0, 1.0,
                 1.0, 1.0,

                 0.0, 0.0,
                 0.0, 1.0,

                 1.0, 0.0,
                 1.0, 1.0,

                 1.0, 0.0,
                 1.0, 0.0,
                 1.0, 1.0,
                 1.0, 1.0,])

(def idxs [0, 1, 3, 3, 1, 2,
           8, 10, 11, 9, 8, 11,
           12, 13, 7, 5, 12, 7,
           14, 15, 6, 4, 14, 6,
           16, 18, 19, 17, 16, 19,
           4, 6, 7, 5, 4, 7,])

(deftype ProjectionEngine [window]
  GameEngine
  (setup [this]
    (let [proj-matrix (camera/->proj-matrix 60.0 1.0 0.01 1000.0)
          vert (shader/compile! (slurp "resources/shaders/vertex/textured.vert") gl/VERTEX_SHADER)
          frag (shader/compile! (slurp "resources/shaders/fragment/textured.frag") gl/FRAGMENT_SHADER)
          program (shader/->program [frag vert])
          texture (texture/->texture "resources/textures/cc-logo.jpg")
          mesh (mesh/->mesh verts idxs)]
      {:cam     proj-matrix
       :program program
       :mesh    mesh
       :texture texture
       :texture-renderer (renderer/->texture-renderer mesh texture tex-coords)
       :scale 1.0
       :tick 1}))
  (nextState [this state]
    (let [{:keys [handle]} window
          {:keys [tick]} state]
      (if (GLFW/glfwWindowShouldClose handle)
        :halt
        (let [scale (ccc/->inspect (Math/sin (Math/toRadians (* 1.5 tick))))]
          (assoc state :scale scale :tick (inc tick))))))
  (render [this {:keys [cam program mesh texture texture-renderer tick scale]}]
    (let [{:keys [handle width height] :as state} window]
      (GLFW/glfwPollEvents)

      (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
      (GL11/glViewport 0 0 width height)

      (let [transform (transform/->transform (Vector3f. 0 0 (+ -10 (* 5 scale))) (Vector3f. (+ 10 tick) (+ 10 tick) (+ 10 tick)) 3)
            uniforms {(shader/create-uniform program "projectionMatrix") cam
                      (shader/create-uniform program "worldMatrix")      (transform/world-matrix transform)
                      (shader/create-uniform program "texture_sampler")  0}
            shader (shader/->shader program uniforms)]

        (.render (TextureRenderer. (:vao texture-renderer) mesh texture) shader))

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