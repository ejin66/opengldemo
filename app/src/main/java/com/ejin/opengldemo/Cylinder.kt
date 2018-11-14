package com.ejin.opengldemo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Cylinder: GLSurfaceView.Renderer {

    private val vertexArray = getVertexes()

    private val vertexShaderCode =
            "attribute vec4 vPosition;" +
            "uniform mat4 vMatrix;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "   gl_Position = vMatrix * vPosition;" +
            "   if (vPosition.z == 0.0) {" +
            "       vColor = vec4(0.9, 0.9, 0.9, 1.0);" +
            "   } else {" +
            "       vColor = vec4(0.0, 0.0, 0.0, 1.0);" +
            "   }" +
            "}"

    private val fragmentShaderCode =
            "precision mediump float;" +
            "varying vec4 vColor;" +
            "void main() {" +
            " gl_FragColor = vColor;" +
            "}"

    private var mProgram = -1

    private val mProjectMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mMatrix = FloatArray(16)

    private val oval = Oval()

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUseProgram(mProgram)
        val positionHandler = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandler)
        GLES20.glVertexAttribPointer(positionHandler, 3, GLES20.GL_FLOAT, false, 0, Util.getFloatBuffer(vertexArray))

        val matrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, mMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexArray.size / 3)
        GLES20.glDisableVertexAttribArray(positionHandler)

        oval.onDrawFrame(gl)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width / height.toFloat()
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1F, 1F, 3F, 10F)
        Matrix.setLookAtM(mViewMatrix, 0, 0F, -5F, 5F, 0F, -1F, 0F, -1F, 1F, 0F)
        Matrix.multiplyMM(mMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)

        oval.setMatrix(mMatrix)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mProgram = Util.createProgram(Util.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode),
                Util.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode))
        GLES20.glClearColor(0.5F, 0.5F, 0.5F, 1F)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        oval.onSurfaceCreated(gl, config)
    }

    private fun getVertexes(): FloatArray {
        val list = mutableListOf<Float>()
        val n = 360
        val radius = 0.5F
        (0..n).forEach {
            val x = radius * Math.cos(2 * Math.PI * it / n).toFloat()
            val y = radius * Math.sin(2 * Math.PI * it / n).toFloat()
            list.add(x)
            list.add(y)
            list.add(-2F)
            list.add(x)
            list.add(y)
            list.add(0F)
        }
        return list.toFloatArray()
    }

}