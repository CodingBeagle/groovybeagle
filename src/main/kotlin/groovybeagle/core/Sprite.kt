package groovybeagle.core

import org.joml.Vector2f

class Sprite constructor(val texture: Texture) {
    var angle: Float = 0.0f
    var position: Vector2f = Vector2f()
    var scale: Vector2f = Vector2f()

    init {
        scale = Vector2f(texture.width, texture.height)
    }
}