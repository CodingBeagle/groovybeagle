package groovybeagle.core

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallback

enum class Key {
    UP_ARROW,
    DOWN_ARROW,
    LEFT_ARROW,
    RIGHT_ARROW,
    SPACE,
    W,
    A,
    S,
    D
}

enum class KeyState {
    PRESSED,
    RELEASED,
    DOWN,
    UP
}

class Input constructor(windowHandle: Long) : NativeResource {
    private val glfwKeysToEngineKeyTable: Map<Int, Key> = mapOf(
        GLFW_KEY_UP to Key.UP_ARROW,
        GLFW_KEY_DOWN to Key.DOWN_ARROW,
        GLFW_KEY_LEFT to Key.LEFT_ARROW,
        GLFW_KEY_RIGHT to Key.RIGHT_ARROW,
        GLFW_KEY_SPACE to Key.SPACE,
        GLFW_KEY_W to Key.W,
        GLFW_KEY_A to Key.A,
        GLFW_KEY_S to Key.S,
        GLFW_KEY_D to Key.D
    )

    private val glfwKeyStateToEngineKeyState: Map<Int, KeyState> = mapOf(
        GLFW_PRESS to KeyState.PRESSED,
        GLFW_RELEASE to KeyState.RELEASED,
        GLFW_REPEAT to KeyState.DOWN
    )

    private val currentKeyStates: Map<Key, KeyState> = mapOf(
        Key.UP_ARROW to KeyState.UP,
        Key.DOWN_ARROW to KeyState.UP,
        Key.LEFT_ARROW to KeyState.UP,
        Key.RIGHT_ARROW to KeyState.UP,
        Key.SPACE to KeyState.UP,
        Key.W to KeyState.UP,
        Key.A to KeyState.UP,
        Key.S to KeyState.UP,
        Key.D to KeyState.UP
    )

    private val theCurrentKeyStates: MutableMap<Key, KeyState> = mutableMapOf()

    private val testLol : MutableMap<Key, KeyState> = mutableMapOf()

    private val glfwKeyCallback: GLFWKeyCallback = GLFWKeyCallback.create { window, key, scancode, action, mods ->
        try
        {
            // We use .getValue on the Kotlin map, as this will return a non-nullable type OR throw an exception
            // if no appropriate key is found. This is good in these cases, as we can react to keys not implemented
            val engineKey = glfwKeysToEngineKeyTable.getValue(key)
            val engineState = glfwKeyStateToEngineKeyState.getValue(action)

            // TODO: Can this be done more elegantly with Kotlin Map API? (I.E, no need for if to replace or put)
            theCurrentKeyStates[engineKey] = engineState

            if (engineState == KeyState.RELEASED)
                testLol.remove(engineKey)
            else
                testLol[engineKey] = KeyState.DOWN

        } catch (e: NoSuchElementException)
        {
            // TODO: Make some nice message about unimplemented key with actual key name instead of int value!
            println(e.message)
        }
    }

    init {
        glfwSetKeyCallback(windowHandle, glfwKeyCallback)
    }

    fun isKeyDown(key: Key): Boolean {
        if (!testLol.containsKey(key))
            return false;

        return testLol[key] == KeyState.DOWN
    }

    fun update() {
        theCurrentKeyStates.clear()
    }

    override fun dispose() {
        glfwKeyCallback.free()
    }
}