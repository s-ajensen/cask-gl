(ns cask.transform
  (:import [org.joml Matrix4f]))

(defrecord Transform [pos rot scale])

(defn ->transform [pos rot scale]
  (->Transform pos rot scale))

(defn world-matrix [{:keys [pos rot scale]}]
  (-> (Matrix4f.)
      .identity
      (.translate pos)
      (.rotateX (float (Math/toRadians (.x rot))))
      (.rotateY (float (Math/toRadians (.y rot))))
      (.rotateZ (float (Math/toRadians (.z rot))))
      (.scale (float scale))))