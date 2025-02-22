(ns cask.api.opengl-spec
  (:require [cask.api.core :as cask]
            [cask.api.opengl :as sut]
            [cask.lwjgl.opengl.gl20 :as gl20]
            [cask.lwjgl.opengl.gl30 :as gl30]
            [cask.lwjgl.system.spec-helper :as mem-helper]
            [cask.opengl.spec-helper :as gl-helper]
            [cask.spec-helper :refer [should-array=]]
            [speclj.core :refer :all])
  (:import (cask.api.opengl GlRenderer)
           (org.joml Matrix4f)))

(def stack (atom {}))

(def tri (float-array (flatten [[0 0 0] [1 0 0] [0 1 0]])))
(def tri-idxs (int-array [0 1 2]))

(describe "OpenGL Cask API implementation"

  (redefs-around [cask/render-impl (delay (GlRenderer.))])

  (with-stubs)
  (mem-helper/stub-mem-stack stack)
  (gl-helper/stub-gl20)
  (gl-helper/stub-gl30)

  (before (gl-helper/clear!))

  (context "mesh loading"

    (it "allocates buffers on GPU"
      (cask/load-mesh! tri tri-idxs)
      (should-array= tri (get @stack [:float 9]))
      (should-array= tri-idxs (get @stack [:int 3])))

    (it "returns mesh pointers & metadata"
      (let [{:keys [vbo ebo size]} (cask/load-mesh! tri tri-idxs)]
        (should= :buf-1 vbo)
        (should= :buf-2 ebo)
        (should= 3 size))))

  (context "shaders"

    (context "compilation"

      (it "fails to create shader"
        (with-redefs [gl20/createShader (stub :createShader {:return 0})]
          (should-throw Exception (str "Error creating shader of type: " gl20/VERTEX_SHADER)
            (cask/compile-shader! "" gl20/VERTEX_SHADER))))

      (it "fails to compile shader"
        (with-redefs [gl20/getShaderi (stub :getShaderi {:return 0})
                      gl20/getShaderInfoLog (stub :getShaderInfoLog {:return "LOG"})]
          (should-throw Exception (str "Error compiling shader:\nLOG")
            (cask/compile-shader! "code" gl20/VERTEX_SHADER))))

      (it "succeeds"
        (should= 1 (cask/compile-shader! "code" gl20/VERTEX_SHADER))
        (should-have-invoked :shaderSource {:with [1 "code"]})
        (should-have-invoked :compileShader {:with [1]})))

    (context "linking"

      (it "fails to create shader"
        (with-redefs [gl20/createProgram (stub :createProgram {:return 0})]
          (should-throw Exception "Could not initialize shader"
            (cask/link-shaders! []))))

      (it "attaches 1 shader"
        (should= :shader-id (cask/link-shaders! [2]))
        (should-have-invoked :attachShader {:with [:shader-id 2]}))

      (it "attaches many shaders"
        (cask/link-shaders! [2 3 4 5])
        (should-have-invoked :attachShader {:with [:shader-id 2]})
        (should-have-invoked :attachShader {:with [:shader-id 3]})
        (should-have-invoked :attachShader {:with [:shader-id 4]})
        (should-have-invoked :attachShader {:with [:shader-id 5]}))

      (it "fails to link program"
        (with-redefs [gl20/getProgrami (stub :getProgrami {:return 0})
                      gl20/getProgramInfoLog (stub :getProgramInfoLog {:return "LOG"})]
          (should-throw Exception "Error linking shader:\nLOG"
            (cask/link-shaders! [2 3]))))

      (it "links shaders"
        (cask/link-shaders! [2 3])
        (should-have-invoked :linkProgram {:with [:shader-id]}))

      (it "detaches linked shaders"
        (cask/link-shaders! [2 3])
        (should-have-invoked :detachShader {:with [:shader-id 2]})
        (should-have-invoked :detachShader {:with [:shader-id 3]})))

    (context "uniforms"

      (context "gets uniform location"

        (it "of nonexistent uniform"
          (with-redefs [gl20/getUniformLocation (stub :getUniformLocation {:return -1})]
            (should-throw Exception "No such uniform 'uniform_name'"
              (cask/get-uniform :shader-id "uniform_name")))
          )

        (it "of existent uniform"
          (should= 1 (cask/get-uniform :shader-id "uniform_name")))
        )

      (context "sets uniform"

        (it "to integer value"
          (cask/set-uniform! :location 1)
          (should= 1 (get @gl-helper/uniforms :location)))

        (it "to matrix value"
          (cask/set-uniform! :location (.identity (Matrix4f.)))
          (should-array= (float-array [1 0 0 0
                                       0 1 0 0
                                       0 0 1 0
                                       0 0 0 1])
                         (get @gl-helper/uniforms :location)))
        )
      )
    )

  (context "load-texture!"

    (it "generates a texture ID"
      (cask/load-texture! 100 100 (byte-array []))
      (should= 1 @gl-helper/cur-texture-id)
      )

    (it "binds texture"
      (cask/load-texture! 100 100 (byte-array []))
      (should= {:target gl30/TEXTURE_2D :id 1} (:bound @gl-helper/textures))
      )

    (it "sets texture parameters"
      (cask/load-texture! 100 100 (byte-array []))
      (should= {gl30/TEXTURE_MIN_FILTER gl30/LINEAR
                gl30/TEXTURE_MAG_FILTER gl30/LINEAR}
               (get-in @gl-helper/textures [:parameters gl30/TEXTURE_2D]))
      )

    (it "sets texture image data"
      (let [test-data (byte-array (range 100))]
        (cask/load-texture! 10 10 test-data)
        (let [image-data (:image-data @gl-helper/textures)
              saved-bytes (byte-array (.remaining (:pixels image-data)))]
          (.get (:pixels image-data) saved-bytes)
          (should= gl30/TEXTURE_2D (:target image-data))
          (should= 0 (:level image-data))
          (should= gl30/RGBA (:internalformat image-data))
          (should= 10 (:width image-data))
          (should= 10 (:height image-data))
          (should= 0 (:border image-data))
          (should= gl30/RGBA (:format image-data))
          (should= gl30/UNSIGNED_BYTE (:type image-data))
          (should-array= test-data saved-bytes)))
      )

    (it "generates mipmaps"
      (cask/load-texture! 100 100 (byte-array []))
      (should= gl30/TEXTURE_2D (:mipmaps-generated @gl-helper/textures))
      )

    (it "returns a texture record"
      (let [result (cask/load-texture! 100 100 (byte-array []))]
        (should= 1 (:tex-id result))
        (should= 100 (:width result))
        (should= 100 (:height result)))
      )
    )
  )
