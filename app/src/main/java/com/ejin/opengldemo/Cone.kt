package com.ejin.opengldemo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Cone : GLSurfaceView.Renderer {

    private val vertexCoords = getCoords()

    private val vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "varying vec4 vColor;" +
                    "uniform mat4 vMatrix;" +
                    "void main() {" +
                    " gl_Position = vMatrix * vPosition;" +
                    " if (vPosition.z != 0.0) {" +
                    "    vColor = vec4(0.0, 0.0, 0.0, 1.0);" +
                    "  } else {" +
                    "    vColor = vec4(0.9, 0.9, 0.9, 1.0);" +
                    "  }" +
                    "}"

    private val fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    " gl_FragColor = vColor;" +
                    "}"

    private var mProjectMatrix = FloatArray(16)
    private var mViewMatrix = FloatArray(16)
    private var mMVPMatrix = FloatArray(16)

    private var mProgram = -1

    private var oval = Oval()

    override fun onDrawFrame(gl: GL10?) {
        Log.d("demo", "onDrawFrame")
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUseProgram(mProgram)

        val positionHandler = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandler)
        GLES20.glVertexAttribPointer(positionHandler, 3, GLES20.GL_FLOAT, false, 0, Util.getFloatBuffer(vertexCoords))

        val matrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, mMVPMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCoords.size / 3)

        GLES20.glDisableVertexAttribArray(positionHandler)

        oval.onDrawFrame(gl)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("demo", "onSurfaceChanged")
        GLES20.glViewport(0, 0, width, height)

        val ratio = width / height.toFloat()
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1F, 1F, 4F, 12F)
        Matrix.setLookAtM(mViewMatrix, 0, -2F, -7F, 3F, 0F, 0F, 0F, 0F, 1F, 0F)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)

        oval.setMatrix(mMVPMatrix)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("demo", "onSurfaceCreated")
        val vertexShader = Util.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = Util.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        mProgram = Util.createProgram(vertexShader, fragmentShader)
        GLES20.glClearColor(0.5F, 0.5F, 0.5F, 1F)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        oval.onSurfaceCreated(gl, config)
    }

    private fun getCoords(): FloatArray {
        val slice = 360
        val radius = 0.5F

        val vertexCoords = mutableListOf(0F, 0F, -1F)
        val z = 0F
        (0..slice).forEach {
            val radian = it * Math.PI * 2 / slice
            val x = (Math.cos(radian) * radius).toFloat()
            val y = (Math.sin(radian) * radius).toFloat()
            vertexCoords.add(x)
            vertexCoords.add(y)
            vertexCoords.add(z)
        }
        return vertexCoords.toFloatArray()
    }

}