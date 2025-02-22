(ns cask.opengl.shader-spec
  (:require [cask.lwjgl.opengl.gl20 :as gl]
            [cask.opengl.spec-helper :as gl-helper]
            [cask.opengl.shader :as sut]
            [speclj.core :refer :all]))

; TODO - these tests need to be _much_ more robust
(describe "Shader"

  (with-stubs)
  (gl-helper/stub-gl20)

  (context "->program"

    (it "fails to create shader"
      (with-redefs [gl/createProgram (stub :createProgram {:return 0})]
        (should-throw Exception "Could not initialize shader"
                      (sut/->program []))))

    #_(it "succeeds"
      (should= 1 (sut/->program []))))

  (context "compile"

    )

  (context "link"

    (it "attaches 1 shader"
      (sut/link! 1 [2])
      (should-have-invoked :attachShader {:with [1 2]}))

    (it "attaches many shaders"
      (sut/link! 1 [2 3 4 5])
      (should-have-invoked :attachShader {:with [1 2]})
      (should-have-invoked :attachShader {:with [1 3]})
      (should-have-invoked :attachShader {:with [1 4]})
      (should-have-invoked :attachShader {:with [1 5]}))

    (it "fails to link program"
      (with-redefs [gl/getProgrami (stub :getProgrami {:return 0})
                    gl/getProgramInfoLog (stub :getProgramInfoLog {:return "LOG"})]
        (should-throw Exception "Error linking shader:\nLOG"
                      (sut/link! 1 [2 3]))))

    (it "links shaders"
      (sut/link! 1 [2 3])
      (should-have-invoked :linkProgram {:with [1]}))

    (it "detaches linked shaders"
      (sut/link! 1 [2 3])
      (should-have-invoked :detachShader {:with [1 2]})
      (should-have-invoked :detachShader {:with [1 3]}))))