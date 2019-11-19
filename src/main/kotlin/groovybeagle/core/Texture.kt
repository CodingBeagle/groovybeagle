package groovybeagle.core

import org.lwjgl.opengl.GL43.*
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack.stackPush

class Texture constructor(imagePath: String) : NativeResource {
    private var didDelete = false

    var width: Float = 0f
        private set

    var height: Float = 0f
        private set

    var textureObject = 0
        private set

    init {
        textureObject = glGenTextures()

        glBindTexture(GL_TEXTURE_2D, textureObject)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,  GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)

        // TODO: Read up on this MIN / MAG filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        /*
            In OpenGL space, the FIRST ROW of the loaded image data will be at the BOTTOM of the image.
            This means that if we don't flip the pixel data when loading, our image will be upside-down when rendering
            with OpenGL, as when loading images with stb_image, the first row will be from the top left of the image.
         */
        stbi_set_flip_vertically_on_load(true)

        val stackFrame = stackPush()
        stackFrame.use {
            val textureWidth = stackFrame.mallocInt(1)
            val textureHeight = stackFrame.mallocInt(1)
            val textureNumberOfChannels = stackFrame.mallocInt(1)

            // If image data is null, stb_image failed to load the image data
            // TODO: Maybe there's a way to get an error string from stb_image?
            val imageData = stbi_load(imagePath, textureWidth, textureHeight, textureNumberOfChannels, STBI_rgb_alpha)
                ?: throw RuntimeException("Failed to load specified texture: $imagePath")

            glTexImage2D(GL_TEXTURE_2D,
                0,
                GL_RGBA,
                textureWidth.get(0), textureHeight.get(0),
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                imageData)

            glGenerateMipmap(GL_TEXTURE_2D)

            width = textureWidth.get(0).toFloat()
            height = textureHeight.get(0).toFloat()

            // Cleanup
            glBindTexture(GL_TEXTURE_2D, 0)
        }
    }

    fun use() {
        if (didDelete)
            throw RuntimeException("Attempting to use texture already deleted.")

        glBindTexture(GL_TEXTURE_2D, textureObject)
    }

    override fun dispose() {
        if (didDelete)
            throw RuntimeException("Attempting to delete a texture already deleted.")

        glDeleteTextures(textureObject)

        didDelete = true;
        textureObject = 0;
    }
}