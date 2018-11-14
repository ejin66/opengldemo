package com.ejin.opengldemo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by j17420 on 2018/4/23.
 * 等腰三角形
 */
class Square : GLSurfaceView.Renderer {

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

    //正方形坐标
    private val triangleCoords = floatArrayOf(
            -0.5f, 0.5f, 0.0f, // top left
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f, // bottom right
            0.5f, 0.5f, 0.0f  // top right
    )

    private val indexs = shortArrayOf(
            0, 1, 2, 0, 2, 3
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
            0F, 0F, 1F, 1F,
            0F, 0F, 1F, 1F
    )

    private val vertexColorBuffer: FloatBuffer

    private val indexBuffer: ShortBuffer

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

        indexBuffer = ByteBuffer.allocateDirect(indexs.size * 2).let {
            it.order(ByteOrder.nativeOrder())
            it.asShortBuffer()
        }.apply {
            put(indexs)
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
        //链接到着色器程序
        //在链接操作执行以后，可以任意修改shader的源代码，对shader重新编译不会影响整个程序，除非重新链接程序。
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

        //加载并使用链接好的程序
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

//            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
        //索引法绘制
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexs.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
        //顶点法绘制
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount)
        //Disable三角形顶点句柄
        GLES20.glDisableVertexAttribArray(mVcHandle)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("Triangle", "onSurfaceChanged")
        GLES20.glViewport(0, 0, width, height)

        val ratio = width / height.toFloat()
        //透视投影矩阵
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1F, 1F, 3F, 7F)
        //相机位置矩阵
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