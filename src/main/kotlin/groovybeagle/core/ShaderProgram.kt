package groovybeagle.core

import org.joml.Matrix4f
import org.lwjgl.opengl.GL43.*
import org.lwjgl.system.MemoryStack.stackPush

class ShaderProgram constructor(vertexShader: Shader, fragmentShader: Shader) : NativeResource {
    private var didDelete = false
    private var shaderProgramObject = 0

    init {
        shaderProgramObject = glCreateProgram()

        glAttachShader(shaderProgramObject, vertexShader.shaderObject)
        glAttachShader(shaderProgramObject, fragmentShader.shaderObject)

        glLinkProgram(shaderProgramObject)

        val stackFrame = stackPush()
        stackFrame.use {
            val didLinkSucceed = stackFrame.mallocInt(1)

            glGetProgramiv(shaderProgramObject, GL_LINK_STATUS, didLinkSucceed)

            if (didLinkSucceed.get(0) == GL_FALSE) {
                val shaderLinkingReport = glGetProgramInfoLog(shaderProgramObject)

                throw RuntimeException("Failed to link shader program with following error $shaderLinkingReport")
            }
        }

        // Cleanup
        vertexShader.dispose()
        fragmentShader.dispose()
    }

    fun use() {
        if (didDelete)
            throw RuntimeException("Attempted to use already deleted shader program.")

        glUseProgram(shaderProgramObject)
    }

    fun setMatrix4(uniformName: String, matrix: Matrix4f) {
        if (didDelete)
            throw RuntimeException("Attempted to use already deleted shader program.")

        val stackFrame = stackPush()
        stackFrame.use {
            val uniformLocation = glGetUniformLocation(shaderProgramObject, uniformName)
            val matrixMemory = matrix.get(stackFrame.mallocFloat(16))
            glUniformMatrix4fv(uniformLocation, false, matrixMemory)
        }
    }

    override fun dispose() {
        if (didDelete)
            throw RuntimeException("Atempting to delete shader program already deleted.")

        glDeleteProgram(shaderProgramObject)

        didDelete = true
        shaderProgramObject = 0
    }
}