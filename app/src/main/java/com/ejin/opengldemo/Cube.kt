package com.ejin.opengldemo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Cube : GLSurfaceView.Renderer {

    private var nested = false
    private val vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec4 aColor;" +
                    "varying vec4 vColor;" +
                    "uniform mat4 vMatrix;" +
                    "void main() {" +
                    "   gl_Position = vMatrix * vPosition;" +
                    "   vColor = aColor;" +
                    "}"

    private val fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "   gl_FragColor = vColor;" +
                    "}"

    private val cubeVertexs = floatArrayOf(
            0.5F, 0.5F, 0.5F,
            0.5F, -0.5F, 0.5F,
            -0.5F, 0.5F, 0.5F,
            -0.5F, -0.5F, 0.5F,
            0.5F, 0.5F, -0.5F,
            0.5F, -0.5F, -0.5F,
            -0.5F, 0.5F, -0.5F,
            -0.5F, -0.5F, -0.5F
    )

    private val vertexColors = floatArrayOf(
            1F, 0F, 0F, 1F,
            0F, 1F, 0F, 1F,
            0F, 0F, 1F, 1F,
            1F, 0F, 0F, 1F,
            0F, 1F, 0F, 1F,
            0F, 0F, 1F, 1F,
            1F, 0F, 0F, 1F,
            0F, 1F, 0F, 1F
    )

    private val indexs = shortArrayOf(
            0, 1, 3, 0, 2, 3,
            4, 5, 6, 5, 7, 6,
            0, 1, 5, 0, 4, 5,
            3, 2, 6, 3, 6, 7,
            2, 0, 4, 2, 4, 6,
            3, 1, 5, 3, 5, 7
    )

    private val vertexBuffer: FloatBuffer = Util.getFloatBuffer(cubeVertexs)

    private val vertexColorBuffer: FloatBuffer = Util.getFloatBuffer(vertexColors)

    private val indexBuffer: ShortBuffer = Util.getShortBuffer(indexs)

    private var mProgram: Int = -1

    private val mProjectMatrix = FloatArray(16)

    private val mViewMatrix = FloatArray(16)

    private var mMVPMatrix = FloatArray(16)

    fun setMatrix(matrix: FloatArray) {
        mMVPMatrix = matrix
        nested = true
    }

    fun getProjectMatrix() = mProjectMatrix

    fun getViewMatrix() = mViewMatrix

    override fun onDrawFrame(gl: GL10?) {
        Log.d("demo", "onDrawFrame")
        //清除信息
        if (!nested)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)

        val positionHandler = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandler)
        GLES20.glVertexAttribPointer(positionHandler, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        val colorHandler = GLES20.glGetAttribLocation(mProgram, "aColor")
        GLES20.glEnableVertexAttribArray(colorHandler)
        GLES20.glVertexAttribPointer(colorHandler, 4, GLES20.GL_FLOAT, false, 0, vertexColorBuffer)

        val matrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, mMVPMatrix, 0)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexs.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        GLES20.glDisableVertexAttribArray(positionHandler)
        GLES20.glDisableVertexAttribArray(colorHandler)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("demo", "onSurfaceChanged")
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width / height.toFloat()

        Matrix.frustumM(mProjectMatrix, 0, -ratio * 2.5F, ratio * 2.5F, -2.5F, 2.5F, 3F, 10F)
        Matrix.setLookAtM(mViewMatrix, 0, 1F, 1F, 7F, 0F, 0F, 0F, 0F, 1F, 0F)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("demo", "onSurfaceChanged")
        mProgram = createProgram()
        GLES20.glClearColor(0.5F, 0.5F, 0.5F, 1F)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    private fun createProgram(): Int {
        Log.d("demo", "create program")
        Log.d("demo", "create vertexShader")
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        Log.d("demo", "create fragmentShader")
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        Log.d("demo", "program: $program")
        return program
    }

}