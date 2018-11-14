package com.ejin.opengldemo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by j17420 on 2018/4/23.
 */
class Triangle : GLSurfaceView.Renderer {

    //顶点着色器
    private val vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "   gl_Position = vPosition;" +
                    "}"

    //片元着色器
    private val fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "   gl_FragColor = vColor;" +
                    "}"

    //三角形坐标
    private val triangleCoords = floatArrayOf(
            0.5f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f
    )

    private val colors = floatArrayOf(
            1.0f, 1.0f, 1.0f, 1.0f
    )

    private val COORDS_PRE_VERTEX = 3
    //顶点个数
    private val vertexCount = triangleCoords.size / COORDS_PRE_VERTEX
    //顶点之间的偏移量
    private val vertexStride = COORDS_PRE_VERTEX * 4

    private val vertexBuffer: FloatBuffer

    private var mProgram: Int = -1

    init {
        vertexBuffer = ByteBuffer.allocateDirect(triangleCoords.size * 4).let {
            it.order(ByteOrder.nativeOrder())
            it.asFloatBuffer()
        }.apply {
            put(triangleCoords)
            position(0)
        }
    }

    private fun createProgram() {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        //创建一个空的OpenGLES程序
        mProgram = GLES20.glCreateProgram()
        //将顶点着色器/片元着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }


    override fun onDrawFrame(gl: GL10?) {
        Log.d("Triangle", "onDrawFrame")
        //使用glClearColor函数所设置的颜色进行清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        //将程序加入到OpenGLES20环境
        GLES20.glUseProgram(mProgram)

        //获取顶点着色器的vPosition成员句柄
        val mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        //启用三角形顶点句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PRE_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)

        val mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, colors, 0)
        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
        //Disable三角形顶点句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("Triangle", "onSurfaceChanged")
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("Triangle", "onSurfaceCreated")
        createProgram()
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
    }
}