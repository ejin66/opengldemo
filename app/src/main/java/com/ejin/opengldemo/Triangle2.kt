package com.ejin.opengldemo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by j17420 on 2018/4/23.
 * 等腰三角形
 */
class Triangle2 : GLSurfaceView.Renderer {

    //顶点着色器
    private val vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 vMatrix;" +
                    "varying vec4 vColor;" +
                    "attribute vec4 aColor;" +
                    "void main() {" +
                    "   gl_Position = vMatrix * vPosition;" +
                    "   vColor = aColor;" +
                    "}"

    //片元着色器
    private val fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
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
    //透视投影矩阵
    private var mProjectMatrix = FloatArray(16)
    //相机位置矩阵
    private var mViewMatrix = FloatArray(16)
    //最后的变换矩阵
    private var mMVPMatrix = FloatArray(16)

    private val vertexColors = floatArrayOf(
            0F, 1F, 0F, 1F,
            1F, 0F, 0F, 1F,
            0F, 0F, 1F, 1F
    )

    private val vertexColorBuffer: FloatBuffer

    init {
        vertexBuffer = ByteBuffer.allocateDirect(triangleCoords.size * 4).let {
            it.order(ByteOrder.nativeOrder())
            it.asFloatBuffer()
        }.apply {
            put(triangleCoords)
            position(0)
        }

        vertexColorBuffer = ByteBuffer.allocateDirect(vertexColors.size * 4).let {
            it.order(ByteOrder.nativeOrder())
            it.asFloatBuffer()
        }.apply {
            put(vertexColors)
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
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        //将程序加入到OpenGLES20环境
        GLES20.glUseProgram(mProgram)

        //获取变换矩阵vMatrix成员句柄
        val mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0)

        //获取顶点着色器的vPosition成员句柄
        val mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        //启用三角形顶点句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PRE_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)

        //获取顶点着色器的aColor成员句柄
        val mVcHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
        //启用句柄
        GLES20.glEnableVertexAttribArray(mVcHandle)
        GLES20.glVertexAttribPointer(mVcHandle, 4, GLES20.GL_FLOAT, false, 0, vertexColorBuffer)

//            val mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
//            //设置绘制三角形的颜色
//            GLES20.glUniform4fv(mColorHandle, 1, colors, 0)
        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        //Disable三角形顶点句柄
        GLES20.glDisableVertexAttribArray(mVcHandle)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("Triangle", "onSurfaceChanged")
        //指定窗口大小
        GLES20.glViewport(0, 0, width, height)

        val ratio = width / height.toFloat()
        //透视投影矩阵
        //[ -ratio, ratio, -1F, 1F]指近面裁剪的区域。即以相机视点中心点为中心，上下左右范围内的视图会投影到近面。
        //near指camera到near面的距离
        //far指camera到far面的距离
        //6个参数设定了一个可见的视锥体，不在该范围内的就裁剪不显示
        //在视锥体内的物体最后会映射到near面？？
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1F, 1F, 3F, 8F)
        //相机位置矩阵
        //eye指相机坐标
        //center指目标的中心点
        //up方向指相机的上方向
        Matrix.setLookAtM(mViewMatrix, 0, 0F, 0F, 7F, 0F, 0F, 0F,
                0F, 1.0F, 0.0F)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("Triangle", "onSurfaceCreated")
        createProgram()
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
    }
}