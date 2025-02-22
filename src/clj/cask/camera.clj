(ns cask.camera
  (:import [org.joml Matrix4f Vector3f]))

(defrecord Camera [transform])

(defn ->proj-matrix [fov-deg aspect z-near z-far]
  (.perspective (Matrix4f.) (Math/toRadians fov-deg) aspect z-near z-far))

(defn ->view-matrix [{:keys [pos rot scale]}]
  (-> (Matrix4f.)
      .identity
      (.rotate (float (Math/toRadians (.x rot))) (Vector3f. 1 0 0))
      (.rotate (float (Math/toRadians (.y rot))) (Vector3f. 0 1 0))
      (.translate (- (.x pos)) (- (.y pos)) (- (.z pos)))))

(defn ->camera [transform]
  (->Camera transform))