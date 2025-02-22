(ns cask.lwjgl.opengl.gl20
  (:import [org.lwjgl.opengl GL20]))

(def VERTEX_SHADER GL20/GL_VERTEX_SHADER)
(def FRAGMENT_SHADER GL20/GL_FRAGMENT_SHADER)
(def COMPILE_STATUS GL20/GL_COMPILE_STATUS)
(def LINK_STATUS GL20/GL_LINK_STATUS)

(defn createProgram []
  (GL20/glCreateProgram))

(defn createShader [shaderType]
  (GL20/glCreateShader shaderType))

(defn shaderSource [shader string]
  (GL20/glShaderSource (int shader) (str string)))

(defn compileShader [shader]
  (GL20/glCompileShader shader))

(defn getShaderi [shader pname]
  (GL20/glGetShaderi shader pname))

(defn getShaderInfoLog [shader maxLength]
  (GL20/glGetShaderInfoLog shader maxLength))

(defn attachShader [program shader]
  (GL20/glAttachShader program shader))

(defn linkProgram [program]
  (GL20/glLinkProgram program))

(defn getProgrami [program pname]
  (GL20/glGetProgrami program pname))

(defn getProgramInfoLog [program maxLength]
  (GL20/glGetProgramInfoLog program maxLength))

(defn getUniformLocation [program name]
  (GL20/glGetUniformLocation (int program) (str name)))

(defn detachShader [program shader]
  (GL20/glDetachShader program shader))

(defn validateProgram [program]
  (GL20/glValidateProgram program))

(defn useProgram [program]
  (GL20/glUseProgram program))

(defn deleteProgram [program]
  (GL20/glDeleteProgram program))

(defn uniformMatrix4fv [location transpose buf]
  (GL20/glUniformMatrix4fv location transpose buf))