package groovybeagle.core

import org.joml.Vector2f
import org.joml.Vector2i

class Sprite constructor(val texture: Texture) {
    var angle: Float = 0.0f
    var position: Vector2f = Vector2f()
    var scale: Vector2f = Vector2f()

    private var didDelete = false

    init {
        scale = Vector2f(texture.width, texture.height)
    }
}