(ns cask.physics-scratch
  (:require [c3kit.apron.corec :as ccc]
            [c3kit.apron.time :as time]
            [cask.api.core :as cask]
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
            [cask.api.opengl]
            [obj-utils.core :as obj])
  (:import (cask.api.opengl GlRenderer)
           (cask.core GameEngine)
           (cask.mesh_renderer ColorRenderer NormalizedRenderer TextureRenderer)
           (org.joml Vector3f)
           [org.lwjgl.glfw Callbacks GLFW GLFWErrorCallback GLFWVidMode]
           [org.lwjgl.opengl GL GL11 GL20 GL30]
           [org.lwjgl.system MemoryStack MemoryUtil]
           [org.ode4j.ode DContact DContactBuffer DContactGeomBuffer DGeom$DNearCallback OdeConstants OdeHelper DGeom DBody DMass DWorld DSpace DContactJoint]))

(defrecord PhysicsState [world space bodies])

(defn create-cube-body [world space position velocity]
  (let [body (OdeHelper/createBody world)
        geom (OdeHelper/createBox space 1.0 1.0 1.0)
        mass (OdeHelper/createMass)]
    (.setBox mass 1.0 1.0 1.0 1.0)
    (.setMass body mass)
    (.setPosition body (nth position 0) (nth position 1) (nth position 2))
    (.setLinearVel body (nth velocity 0) (nth velocity 1) (nth velocity 2))
    (.setBody geom body)
    {:body body :geom geom}))

(defn create-static-geom [space position dimensions]
  (let [geom (doto (OdeHelper/createBox space (nth dimensions 0) (nth dimensions 1) (nth dimensions 2))
               (.setPosition (nth position 0) (nth position 1) (nth position 2)))]
    {:geom geom :static true}))

(defn setup-physics []
  (let [world (OdeHelper/createWorld)
        space (OdeHelper/createHashSpace)
        body1 (create-cube-body world space [0 5 0] [0.5 0 0])
        body2 (create-cube-body world space [-2 7 0] [-0.5 0 0])
        floor (create-static-geom space [0 -5 0] [20 20 20])]
    (.setGravity world 0 -9.81 0)
    (->PhysicsState world space [body1 body2 floor])))

(defn handle-collisions [world space]
  (OdeHelper/spaceCollide space nil
                          (reify DGeom$DNearCallback
                            (call [this data o1 o2]
                              (let [b1 (.getBody o1)
                                    b2 (.getBody o2)]
                                (when (or b1 b2)
                                  (let [contacts (DContactGeomBuffer. 1)]
                                    (when (> (OdeHelper/collide o1 o2 1 contacts) 0)
                                      (let [contact (DContact.)
                                            contact-geom (.get contacts 0)]
                                        (.set (.getContactGeom contact) contact-geom)
                                        (let [surface (.-surface contact)]
                                          (set! (.-mode surface)
                                                (bit-or (.-mode surface)
                                                        OdeConstants/dContactBounce
                                                        OdeConstants/dContactSoftERP))
                                          (set! (.-bounce surface) 0.9)
                                          (set! (.-soft_erp surface) 0.2))
                                        (let [joint (OdeHelper/createContactJoint world nil contact)]
                                          (if b1
                                            (.attach joint b1 b2)
                                            (.attach joint b2 b1))))))))))))

(def colors [0.5, 0.0, 0.0,
             0.0, 0.5, 0.0,
             0.0, 0.0, 0.5,
             0.0, 0.5, 0.5,
             0.5, 0.0, 0.0,
             0.0, 0.5, 0.0,
             0.0, 0.0, 0.5,
             0.0, 0.5, 0.5,
             0.5, 0.0, 0.0,
             0.0, 0.5, 0.0,
             0.0, 0.0, 0.5,
             0.0, 0.5, 0.5,
             0.5, 0.0, 0.0,
             0.0, 0.5, 0.0,
             0.0, 0.0, 0.5,
             0.0, 0.5, 0.5,
             0.5, 0.0, 0.0,
             0.0, 0.5, 0.0,
             0.0, 0.0, 0.5,
             0.0, 0.5, 0.5,
             0.5, 0.0, 0.0,
             0.0, 0.5, 0.0,
             0.0, 0.0, 0.5,
             0.0, 0.5, 0.5,
             0.5, 0.0, 0.0,
             0.0, 0.5, 0.0,
             0.0, 0.0, 0.5,
             0.0, 0.5, 0.5,
             0.5, 0.0, 0.0,
             0.0, 0.5, 0.0,
             0.0, 0.0, 0.5,
             0.0, 0.5, 0.5,])

(def renderer (delay (GlRenderer.)))

(deftype ProjectionEngine [window]
  GameEngine
  (setup [this]
    (OdeHelper/initODE2 0)
    (let [camera-position (Vector3f. 0 8 20)
          camera-rotation (Vector3f. 13 0.8 0)
          camera-scale (Vector3f. 1 1 1)
          camera-transform (transform/->transform camera-position camera-rotation camera-scale)

          vert (cask/compile-shader! renderer (slurp "resources/shaders/vertex/projected.vert") gl/VERTEX_SHADER)
          frag (cask/compile-shader! renderer (slurp "resources/shaders/fragment/projected.frag") gl/FRAGMENT_SHADER)
          ;texture (texture/->texture "resources/textures/cc-logo.jpg")

          cube-obj (obj/align-idxs (obj/parse (slurp "resources/models/cube.obj")))
          cube-mesh (mesh/->mesh (:vertices cube-obj) (:idxs cube-obj))
          floor-obj (obj/align-idxs (obj/parse (slurp "resources/models/cube.obj")))
          floor-mesh (mesh/->mesh (:vertices floor-obj) (:idxs floor-obj))

          physics (setup-physics)
          initial-transforms (mapv (fn [{:keys [body geom static]}]
                                     (let [pos (if body
                                                 (.getPosition body)
                                                 (.getPosition geom))]
                                       (transform/->transform
                                         (Vector3f. (.get0 pos) (.get1 pos) (.get2 pos))
                                         (Vector3f. 0 0 0)
                                         (if static
                                           20
                                           1))))
                                   (:bodies physics))]
      {:proj-matrix (camera/->proj-matrix 60.0 1.0 0.01 1000.0)
       :program     (cask/link-shaders! renderer [frag vert])
       :cam         (camera/->camera camera-transform)
       :cube-mesh   cube-mesh
       :cube-obj    cube-obj
       :floor-mesh  floor-mesh
       :floor-obj   floor-obj
       ;:texture     texture
       :transforms  initial-transforms
       :renderer    (renderer/->color-renderer cube-mesh colors)
       :floor-renderer (renderer/->color-renderer floor-mesh colors)
       :scale       1.0
       :tick        1
       :physics     physics}))

  (nextState [this state]
    (let [{:keys [handle]} window
          {:keys [tick physics]} state]
      (if (GLFW/glfwWindowShouldClose handle)
        :halt
        (do
          (handle-collisions (:world physics) (:space physics))
          (.step (:world physics) 0.016)
          (let [scale (inc (Math/sin (Math/toRadians (* 1.5 tick))))
                new-transforms (mapv (fn [{:keys [body geom static]} old-transform]
                                       (if static
                                         old-transform
                                         (let [pos (.getPosition body)]
                                           (transform/->transform
                                             (Vector3f. (.get0 pos) (.get1 pos) (.get2 pos))
                                             (Vector3f. 1 1 1)
                                             1))))
                                     (:bodies physics)
                                     (:transforms state))]
            (assoc state
              :scale scale
              :tick (inc tick)
              :transforms new-transforms))))))

  (render [this {:keys [proj-matrix program cam cube-obj cube-mesh floor-obj floor-mesh texture renderer floor-renderer transforms]}]
    (let [{:keys [handle width height] :as state} window]
      (GLFW/glfwPollEvents)

      (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
      (GL11/glViewport 0 0 width height)

      (doseq [[index transform] (map-indexed vector transforms)]
        (let [uniforms {(shader/create-uniform program "projectionMatrix") proj-matrix
                        (shader/create-uniform program "modelViewMatrix")  (renderer/model-view-matrix transform cam)
                        ;(shader/create-uniform program "texture_sampler")  0
                        }
              shader (shader/->shader program uniforms)]

          (if (= index 2)
            (.render (ColorRenderer. (:vao floor-renderer) floor-mesh colors) shader)
            (.render (ColorRenderer. (:vao renderer) cube-mesh colors) shader))))

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
        (OdeHelper/closeODE)
        (.free (GLFW/glfwSetErrorCallback nil))))))