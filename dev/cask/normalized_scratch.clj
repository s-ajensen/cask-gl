(ns cask.normalized-scratch
  (:require [c3kit.apron.corec :as ccc]
            [c3kit.apron.time :as time]
            [cask.glfw :as window]
            [cask.lwjgl.glfw.glfw :as glfw]
            [cask.camera :as camera]
            [cask.core :as core]
            [cask.mesh-renderer :as renderer]
            [cask.texture :as texture]
            [cask.transform :as transform]
            [cask.lwjgl.opengl.gl20 :as gl]
            [cask.mesh :as mesh]
            [cask.opengl.shader :as shader]
            [obj-utils.core :as obj])
  (:import (cask.core GameEngine)
           (cask.mesh_renderer ColorRenderer NormalizedRenderer TextureRenderer)
           (org.joml Vector3f)
           [org.lwjgl.glfw Callbacks GLFW GLFWErrorCallback GLFWVidMode]
           [org.lwjgl.opengl GL GL11 GL20 GL30]
           [org.lwjgl.system MemoryStack MemoryUtil]))

(deftype ProjectionEngine [window]
  GameEngine
  (setup [this]
    (let [proj-matrix (camera/->proj-matrix 60.0 1.0 0.01 1000.0)
          vert (shader/compile! (slurp "resources/shaders/vertex/normalized.vert") gl/VERTEX_SHADER)
          frag (shader/compile! (slurp "resources/shaders/fragment/normalized.frag") gl/FRAGMENT_SHADER)
          program (shader/->program [frag vert])
          texture (texture/->texture "resources/textures/cc-logo.jpg")
          obj (obj/align-idxs (obj/parse (slurp "resources/models/teapot.obj")))
          mesh (mesh/->mesh (:vertices obj) (:idxs obj))]
      {:proj-matrix proj-matrix
       :program     program
       :cam         (camera/->camera (transform/->transform (Vector3f. 0 0 0) (Vector3f. 0 0 0) 1.0))
       :mesh        mesh
       :obj         obj
       :texture     texture
       :transform   (transform/->transform (Vector3f. 0 0 -3) (Vector3f. 0 0 0) 1)
       :renderer    (renderer/->normalized-renderer mesh texture (:tex-coords obj) (:normals obj))
       :scale       1.0
       :tick        1}))
  (nextState [this state]
    (let [{:keys [handle]} window
          {:keys [tick]} state]
      (if (GLFW/glfwWindowShouldClose handle)
        :halt
        (let [scale (inc (Math/sin (Math/toRadians (* 1.5 tick))))]
          (assoc state :scale scale
                       :tick (inc tick)
                       :cam (camera/->camera (transform/->transform (Vector3f. 0 (* 2 (dec scale)) 0) (Vector3f. (* 10 (dec scale)) 0 0) 1.0))
                       :transform (transform/->transform (Vector3f. 0 0 -30) (Vector3f. tick tick tick) (* 0.5 (inc scale)))
                       )))))
  (render [this {:keys [proj-matrix program cam obj mesh texture renderer transform]}]
    (let [{:keys [handle width height] :as state} window]
      (GLFW/glfwPollEvents)

      (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
      (GL11/glViewport 0 0 width height)

      (let [uniforms {(shader/create-uniform program "projectionMatrix") proj-matrix
                      (shader/create-uniform program "modelViewMatrix")  (renderer/model-view-matrix transform cam)
                      (shader/create-uniform program "texture_sampler")  0}
            shader (shader/->shader program uniforms)]

        (.render (NormalizedRenderer. (:vao renderer) mesh texture (:normals obj)) shader))

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