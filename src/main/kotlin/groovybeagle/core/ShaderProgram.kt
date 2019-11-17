package groovybeagle.core

import org.joml.Matrix4f
import org.lwjgl.opengl.GL43.*
import org.lwjgl.system.MemoryStack.stackPush

class ShaderProgram {
    private var didDelete = false
    private var shaderProgramObject = 0

    fun create(vertexShader: Shader, fragmentShader: Shader) {
        if (didDelete)
            throw RuntimeException("Attempted to use already deleted shader program.")

        if (shaderProgramObject != 0)
            throw RuntimeException("Attmpted to create a shader program already created.")

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
    }

    fun use() {
        if (didDelete)
            throw RuntimeException("Attempted to use already deleted shader program.")

        if (shaderProgramObject == 0)
            throw RuntimeException("Attempting to use a shader program not yet created.")

        glUseProgram(shaderProgramObject)
    }

    fun setMatrix4(uniformName: String, matrix: Matrix4f) {
        if (didDelete)
            throw RuntimeException("Attempted to use already deleted shader program.")

        if (shaderProgramObject == 0)
            throw RuntimeException("Attempting to use a shader program not yet created.")

        val stackFrame = stackPush()
        stackFrame.use {
            val uniformLocation = glGetUniformLocation(shaderProgramObject, uniformName)
            val matrixMemory = matrix.get(stackFrame.mallocFloat(16))
            glUniformMatrix4fv(uniformLocation, false, matrixMemory)
        }
    }

    fun delete() {
        if (didDelete)
            throw RuntimeException("Atempting to delete shader program already deleted.")

        glDeleteProgram(shaderProgramObject)

        didDelete = true
        shaderProgramObject = 0
    }
}