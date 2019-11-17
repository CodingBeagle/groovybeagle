package groovybeagle.core

import org.lwjgl.opengl.GL43.*
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack.stackPush

class Texture {
    var didDelete = false
    var textureObject = 0

    fun loadFromImage(imagePath: String) {
        if (didDelete)
            throw RuntimeException("Attempting to use texture already deleted.")

        if (textureObject != 0)
            throw RuntimeException("Attempting to load a texture already in use.")

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

            // Cleanup
            glBindTexture(GL_TEXTURE_2D, 0)
        }
    }

    fun use() {
        if (didDelete)
            throw RuntimeException("Attempting to use texture already deleted.")

        if (textureObject == 0)
            throw RuntimeException("Attempting to use texture not yet created.")

        glBindTexture(GL_TEXTURE_2D, textureObject)
    }

    fun delete() {
        if (didDelete)
            throw RuntimeException("Attempting to delete a texture already deleted.")

        glDeleteTextures(textureObject)

        didDelete = true;
        textureObject = 0;
    }
}