import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.*

import org.lwjgl.system.MemoryStack.*
import org.lwjgl.opengl.GL11.*


var windowHandle: Long = 0

fun main() {
    println("LWJGL Version: " + Version.getVersion() + "!")

    init()
    loop()
}

/*
    Functions in Kotlin are declared using the "fun" keyword.

    When a function does not return any useful value, its return type is "Unit". Unit is a type with only one value: Unit.
    A function whos return type is Unit will not have to declare it explicitly, it's simply implicit.

    The Unit object corresponds to "void" in languages like Java.
 */
fun init() {
    // Setup an error callback. The default implementation
    // will print the error message in System.err.
    GLFWErrorCallback.createPrint(System.err).set()

    // Initialize GLFW.
    // This has to be done before any other GLFW functions can be used.
    if ( !glfwInit() ) {
        throw RuntimeException("Failed to initialize GLFW!")
    }

    // Configure GLFW
    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // The window will stay hidden after creation
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE) // The window will not be resizeable

    // Create the window
    windowHandle = glfwCreateWindow(800, 600, "Groovy Beagle Engine!", 0, 0)
    if (windowHandle == 0L)
        throw RuntimeException("Failed to create window!")

    // Setup a key callback. It will be called every time a key is pressed, repeated, or released.
    glfwSetKeyCallback(windowHandle) { window, key, scancode, action, mods ->
        if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
            glfwSetWindowShouldClose(windowHandle, true)
    }

    // Get the thread stack and push a new frame
    /*
        Kotlin has a generic extension on all "Closeable?" types.
        This type is used for Java's try-with-resources.
        This function takes a function literal block which gets executed in a try. Afterwards, the object will
        automatically be closed in a "finally".
     */
    val stack = stackPush();
    stack.use {
        val pWidth = stack.mallocInt(1) // int*
        val pHeight = stack.mallocInt(1) // int*

        // Get the window size passed to glfwCreateWindow
        glfwGetWindowSize(windowHandle, pWidth, pHeight)

        // Get the resolution of the primary monitor
        // Here we use the elvis operator ?:
        // It will evaluate the expression on the right side in the case that the left side is null.
        // We do this because the rest of the code in this block does not make sense if we fail to retrieve the
        // vidMode
        val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor()) ?: throw RuntimeException("Failed to retrieve video mode.");

        // Center the window
        glfwSetWindowPos(
            windowHandle,
            (vidMode.width() - pWidth.get(0)) / 2,
            (vidMode.height() - pHeight.get(0)) / 2
        )
    } // Stack frame is popped automatically at end of use

    // Make the OpenGL context current
    glfwMakeContextCurrent(windowHandle)

    // Enable v-sync
    glfwSwapInterval(1)

    // Make the window visible
    glfwShowWindow(windowHandle)
}

fun loop() {
    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL
    // bindings available for use.
    GL.createCapabilities()

    // Set the clear color
    glClearColor(1.0f, 0.0f, 0.0f, 0.0f)

    while (!glfwWindowShouldClose(windowHandle)) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        glfwSwapBuffers(windowHandle);

        glfwPollEvents()
    }
}

