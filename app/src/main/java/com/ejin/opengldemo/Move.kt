package com.ejin.opengldemo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Move: GLSurfaceView.Renderer {

    private val cube = Cube()

    private var currentMatrix = floatArrayOf(
            1F, 0F, 0F, 0F,
            0F, 1F, 0F, 0F,
            0F, 0F, 1F, 0F,
            0F, 0F, 0F, 1F
    )

    private val stack = Stack<FloatArray>()

    private var projectMatrix = FloatArray(16)

    private var viewMatrix = FloatArray(16)

    private fun push() {
        stack.push(currentMatrix.copyOf())
    }

    private fun pop() {
        currentMatrix = stack.pop()
    }

    private fun getFinalMatrix(): FloatArray {
        val result = FloatArray(16)
        Matrix.multiplyMM(result, 0, viewMatrix, 0, currentMatrix, 0)
        Matrix.multiplyMM(result, 0, projectMatrix, 0, result, 0)
        return result
    }


    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        push()
        cube.setMatrix(getFinalMatrix())
        cube.onDrawFrame(gl)
        pop()

        push()
        //已（0,0,0）-> (x,y,z)的向量方向为轴，逆时针旋转
        Matrix.rotateM(currentMatrix, 0, 30F, 0F, -1F, 0F)
        //位移（x,y,z）的倍数
        Matrix.translateM(currentMatrix, 0, 0F, 1.5F, 0F)
        cube.setMatrix(getFinalMatrix())
        cube.onDrawFrame(gl)
        pop()

        push()
        Matrix.rotateM(currentMatrix, 0, 30F, 0F, 1F, 0F)
        Matrix.translateM(currentMatrix, 0, 0F, -1.5F, 0F)
        cube.setMatrix(getFinalMatrix())
        cube.onDrawFrame(gl)
        pop()

        push()
        Matrix.translateM(currentMatrix, 0, 1.5F, 0F, 0F)
        //(X,Y,Z)分别缩放的倍数
        Matrix.scaleM(currentMatrix, 0, 1.2F, 1.2F, 1.2F)
        Matrix.rotateM(currentMatrix, 0, 180F, 0F, 1F, 0F)
        cube.setMatrix(getFinalMatrix())
        cube.onDrawFrame(gl)
        pop()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        cube.onSurfaceChanged(gl, width, height)
        projectMatrix = cube.getProjectMatrix()
        viewMatrix = cube.getViewMatrix()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        cube.onSurfaceCreated(gl, config)
    }
}