(ns cask.glfw-spec
  (:require [cask.glfw :as sut]
            [cask.lwjgl.glfw.glfw :as glfw]
            [cask.lwjgl.glfw.spec-helper :as glfw-helper]
            [speclj.core :refer :all]))

(describe "GLFW"

  (with-stubs)
  (glfw-helper/stub-glfw)

  (redefs-around [sut/position-window (stub :position-window)])

  (before (sut/init))

  (it "fails to initialize GLFW"
    (with-redefs [glfw/init (stub :glfwInit {:throw IllegalStateException})]
     (should-throw (sut/init))))

  (context "window hints"

    (it "uses default hints"
      (should-have-invoked :glfwDefaultWindowHints))

    (it "hides window before initialization"
      (should-have-invoked :glfwWindowHint {:with [glfw/VISIBLE 0]}))

    (it "allows window resizing"
      (should-have-invoked :glfwWindowHint {:with [glfw/RESIZABLE 1]})))

  (context "window creation"

    (it "fails"
      (with-redefs [glfw/createWindow (stub :glfwCreateWindow {:return 0})]
        (should-throw (sut/init))))

    (context "success"

      (it "default values"
        (should-have-invoked :glfwCreateWindow {:with [300 300 "Cask" 0 0]}))

      (it "specifies width & height"
        (sut/init 200 100)
        (should-have-invoked :glfwCreateWindow {:with [200 100 "Cask" 0 0]}))

      (it "window title"
        (sut/init 200 100 "Title")
        (should-have-invoked :glfwCreateWindow {:with [200 100 "Title" 0 0]}))

      (it "configures window"
        (should-have-invoked :position-window)
        (should-have-invoked :glfwMakeContextCurrent {:with [1]})
        (should-have-invoked :glfwSwapInterval {:with [0]})
        (should-have-invoked :glfwShowWindow {:with [1]}))

      (it "stores window pointer"
        (should= 1 (:handle (sut/init)))))))