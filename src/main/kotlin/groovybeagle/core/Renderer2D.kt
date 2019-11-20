package groovybeagle.core

import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL43.*
import org.lwjgl.system.MemoryUtil
import java.io.File

class Renderer2D {
    // TODO: Read more about Kotlin's "primitive type arrays"
    private var vertices: FloatArray = floatArrayOf(
        -1.0f, -1.0f, 0.0f, 0.0f, // Bottom Left
         1.0f, -1.0f, 1.0f, 0.0f, // Bottom Right
         1.0f,  1.0f, 1.0f, 1.0f, // Top Right
        -1.0f,  1.0f, 0.0f, 1.0f  // Top Left
        )

    private var indices = intArrayOf(
        0, 1, 2,
        0, 2, 3
    )

    private var shaderProgram: ShaderProgram

    init {
        // Enable transparency
        // GL_BLEND = If enabled, blend the computed fragment color values with the values in the color buffers.
        // Blending is initially disabled, so we have to enable it manually.
        glEnable(GL_BLEND)

        // In RGBA mode, pixels can be drawn using a function that blends the incoming (source) RGBA values with the RGBA values already in the frame buffer (destination values).
        // Parameter 1 = sfactor
        // Specifies how the red, green, blue, and alpha source blending factors are computed.
        // Parameter 2 = dfactor
        // Specifies how the red, green, blue, and alpha destination blending factors are computed.
        // The best blend function for implementing transparency is (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA) with primitives sorted from farthest to nearest.
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Set background color
        glClearColor(0.39f, 0.58f, 0.93f, 1.0f)

        // Upload Quad vertex data

        // A Vertex buffer Object (VBO) is the common term for a buffer object when it is used
        // as a source for vertex array data.
        // It's not that it's a special type of buffer. It's just a regular buffer object.
        // It's the usage of the buffer for vertex data that gives it its name.
        var quadVBO = glGenBuffers()

        // glBindBuffer will bind a buffer object to a specified buffer binding pointer.
        // GL_ARRAY_BUFFER is used for VBO's (Vertex Buffer Objects).
        glBindBuffer(GL_ARRAY_BUFFER, quadVBO)

        /*
            We make use of LWJGLs MemoryUtil API.

            We can use this API when stack allocation (using the Stack API) using ideal. The memory might be
            too big for the stack, or the allocation might have to be long lived.

            The next best option in that case is explicit memory management, which can be done with the MemoryUtil API.

            The reason we make use of the MemoryUtil API in order to create buffers that wraps around our vertices
            and indices arrays is that LWJGL requires the use of off-heap memory when passing data to native libraries.
            Any buffers returned from native libraries are also always backed by off-heap memory.

            Reasons are:
            - You cannot control the layout of Java objects. Different JVMs and different JVM settings
            produce very different field layouts. Native libraries always expect data with very precisely defined
            layouts.

            - Any Java object or array may be moved by the GC at any time, concurrently with the execution of a native
            method call. All JNI methods are executed at a safepoint, so by definition, must not access heap data.
         */
        var verticesBuffer = MemoryUtil.memAllocFloat(vertices.size)
        verticesBuffer.put(vertices)
        verticesBuffer.flip()

        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)

        MemoryUtil.memFree(verticesBuffer)

        // A Vertex Array Object is an OpenGL object that stores all state needed to supply vertex data.
        // It will store:
        // - The format of the vertex data (through calls of glVertexAttribPointer)
        // - The buffer objects used (which indirectly happens through calls to glVertexAttribPointer)
        // - Element Array Buffer Bindings
        var quadVAO = glGenVertexArrays()
        glBindVertexArray(quadVAO)

        // An Element Array Buffer is a buffer used to store indices for rendering primitives
        var quadVEO = glGenBuffers()

        // We bind it with our currently bound VAO, because its state is saved with it
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,  quadVEO)

        var indicesBuffer = MemoryUtil.memAllocInt(indices.size)
        indicesBuffer.put(indices)
        indicesBuffer.flip()

        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)

        MemoryUtil.memFree(indicesBuffer)

        // When calling glVertexAttribPointer, it uses whatever buffer is currently bound to GL_ARRAY_BUFFER target
        // as the source for the vertex array data.
        // This, binding a NEW buffer to GL_ARRAY_BUFFER after this call will do NOTHING to the association between
        // attribute at index 0 and its source. You'd have to rebind the buffer and make ANOTHER call to glVertexAttribPointer
        // for that to happen.
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * 4, 0)
        glEnableVertexAttribArray(0)

        // Cleanup
        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
        glDisableVertexAttribArray(0)

        // Compile shaders
        var vertexShaderSourceCode = File("engineResources/shaders/vert.glsl").readText()
        var fragmentShaderSourceCode = File("engineResources/shaders/frag.glsl").readText()

        shaderProgram = ShaderProgram(
            Shader(vertexShaderSourceCode, GL_VERTEX_SHADER),
            Shader(fragmentShaderSourceCode, GL_FRAGMENT_SHADER))

        shaderProgram.use()

        glBindVertexArray(quadVAO)

        // Set up orthographic projection
        // For an orthographic projection, the viewing volume is a rectangular box. Unlike perspective projection, the size of the viewing volume doesn't
        // change from one end to the other, which means that distance from the camera won't affect how large an object appears.
        // An orthographic projection instead takes this view volume (the rectangular box) and transforms it into the unit cube.
        // A common matrix for performing the orthographic projection is expressed in terms of the six-tuple: (left, right, bottom, top, near, far).
        // The minimum corner is: (left, bottom, near)
        // The maximum corner is: (right, top, far)
        // Thus, the entities in our 2D world will be specified in terms of positions in the width and height of our window, and these will then,
        // by this orthographic project matrix, be scaled down into the unit cube that OpenGL will clip against.
        var projection = Matrix4f()
            .ortho(0.0f, 800.0f, 0.0f, 600.0f, -1.0f, 1.0f)

        shaderProgram.setMatrix4("projection", projection)
    }

    fun drawSprite(sprite: Sprite) {
        shaderProgram.use()

        sprite.texture.use()

        val modelMatrix = Matrix4f()
            .identity()
            .translate(sprite.position.x, sprite.position.y, 0.0f)
            .rotate(sprite.angle, Vector3f(0.0f, 0.0f, -1.0f))
            .scale(sprite.scale.x * 0.5f, sprite.scale.y * 0.5f, 1.0f)

        shaderProgram.setMatrix4("model", modelMatrix)

        glDrawElements(
            GL_TRIANGLES,
            6,
            GL11.GL_UNSIGNED_INT,
            0L
        )

        // Cleanup
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}