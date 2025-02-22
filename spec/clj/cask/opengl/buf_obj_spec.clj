(ns cask.opengl.buf-obj-spec
  (:require [cask.lwjgl.opengl.gl30 :as gl30]
            [cask.lwjgl.system.spec-helper :as mem-helper]
            [cask.opengl.spec-helper :as gl-helper]
            [cask.opengl.buf-obj :as sut]
            [cask.spec-helper :refer [should-array=]]
            [speclj.core :refer :all]))

(def stack (atom {}))

(declare ^:dynamic vao-id)
(declare ^:dynamic vbo-id)
(declare ^:dynamic ebo-id)

(describe "Buffer object utils"

  (with-stubs)
  (mem-helper/stub-mem-stack stack)
  (gl-helper/stub-gl30)

  (before (gl-helper/clear!))

  (context "loads"

    (it "vbo"
      (let [data (float-array [1 2 3 4])]
        (should= :buf-1 (sut/load-vbo! stack data))
        (should-array= data (:data (get @gl-helper/buffer-data :buf-1)))
        (should-array= data (get @stack [:float 4]))
        (should= gl30/STATIC_DRAW (:usage (get @gl-helper/buffer-data :buf-1)))
        (should= 0 (get @gl-helper/buffers gl30/ARRAY_BUFFER)))
      )

    (it "ebo"
      (let [data (int-array [1 2 3 4])]
        (should= :buf-1 (sut/load-ebo! stack data))
        (should-array= data (:data (get @gl-helper/buffer-data :buf-1)))
        (should-array= data (get @stack [:int 4]))
        (should= gl30/STATIC_DRAW (:usage (get @gl-helper/buffer-data :buf-1)))
        (should= 0 (get @gl-helper/buffers gl30/ELEMENT_ARRAY_BUFFER))))
    )

  (context "vao"

    (context "binding"

      (with vao-id (gl30/genVertexArrays))

      (context "vbo"

        (with vbo-id (gl30/genBuffers))

        (it "enables vertex attribute array at position"
          (let [vbo (sut/->Vbo @vbo-id 3)]
            (sut/assign-vbo! @vao-id vbo 0)
            (should (get-in @gl-helper/vaos [@vao-id :vbos @vbo-id :enabled])))
          )

        (it "binds vbo to vao"
          (let [vbo (sut/->Vbo @vbo-id 3)]
            (sut/assign-vbo! @vao-id vbo 0)
            (should-contain @vbo-id (get-in @gl-helper/vaos [@vao-id :vbos])))
          )

        (it "sets vbo length"
          (let [vbo (sut/->Vbo @vbo-id 2)]
            (sut/assign-vbo! @vao-id vbo 0)
            (should= 2 (get-in @gl-helper/vaos [@vao-id :vbos :buf-1 :size])))
          )

        (it "cleans up buffers objects"
          (let [vbo (sut/->Vbo vbo-id 2)]
            (sut/assign-vbo! @vao-id vbo 0)
            (should= 0 (get @gl-helper/buffers gl30/ARRAY_BUFFER))
            (should= 0 @gl-helper/vao)))
        )

      (context "ebo"

        (with ebo-id (gl30/genBuffers))

        (it "binds ebo to vao"
          (let [vbo (sut/->Ebo @ebo-id)]
            (sut/assign-ebo! @vao-id vbo)
            (should-contain @ebo-id (get-in @gl-helper/vaos [@vao-id :vbos])))
          )

        (it "cleans up vao"
          (let [vbo (sut/->Ebo @ebo-id)]
            (sut/assign-ebo! @vao-id vbo)
            (should= 0 @gl-helper/vao))
          )
        )
      )
    )
  )