(ns cask.spec-helper
  (:require [speclj.core :refer :all]))

(defmacro should-array= [arr1 arr2]
  `(let [arr1# ~arr1
         arr2# ~arr2]
     (should= (seq arr1#) (seq arr2#))))