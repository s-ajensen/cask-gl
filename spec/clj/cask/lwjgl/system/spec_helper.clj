(ns cask.lwjgl.system.spec-helper
  (:require [c3kit.apron.corec :as ccc]
            [cask.lwjgl.system.memory-stack :as mem-stack]
            [speclj.core :refer [before redefs-around stub]])
  (:import (clojure.lang IAtom IDeref)
           (java.io Closeable)))

(deftype FakeStack [atom]
  Closeable
  (close [_] (ccc/noop))
  IDeref
  (deref [_] @atom)
  IAtom
  (swap [this f] (FakeStack. (swap! atom f)))
  (swap [this f a] (FakeStack. (swap! atom f a)))
  (swap [this f a b] (FakeStack. (swap! atom f a b)))
  (swap [this f a b more] (FakeStack. (apply swap! atom f a b more)))
  (reset [this newval] (FakeStack. (reset! atom newval))))

(defn calloc-float [stack size]
  (let [buffer (float-array size)]
    (swap! stack assoc [:float size] buffer)
    buffer))

(defn calloc-int [stack size]
  (let [buffer (int-array size)]
    (swap! stack assoc [:int size] buffer)
    buffer))

(defn put-data! [buffer data]
  (System/arraycopy data 0 buffer 0 (count data)))

(defn get-allocated [stack type size]
  (get @stack [type size]))

(defn stub-mem-stack [stack]
  (before (reset! stack {}))
  (redefs-around [mem-stack/stackPush (stub :stackPush {:return (->FakeStack stack)})
                  mem-stack/mallocFloat (stub :mallocFloat {:invoke calloc-float})
                  mem-stack/callocFloat (stub :callocFloat {:invoke calloc-float})
                  mem-stack/callocInt (stub :callocInt {:invoke calloc-int})
                  mem-stack/put (stub :put {:invoke (fn [buffer _ data] (put-data! buffer data))})]))