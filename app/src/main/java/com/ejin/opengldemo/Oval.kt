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
class Oval : GLSurfaceView.Renderer {

    /*
    attribute ：使用顶点数组封装每个顶点的数据，一般用于每个顶点都各不相同的变量，如顶点位置、颜色等
            只读的顶点数据，只能用在顶点着色器中，一个attribute可以是浮点数类型的标量，向量，或者矩阵。不可以是数组或则结构体
    uniform : 顶点着色器使用的常量数据，不能被着色器修改，一般用于对同一组顶点组成的单个3D物体中所有顶点都相同的变量，如当前光源的位置。
            且它在顶点着色器与片元着色器中可以实现共享。
    sampler:这个是可选的，一种特殊的uniform，表示顶点着色器使用的纹理。
    mat4: 表示4×4浮点数矩阵，该变量存储了组合模型视图和投影矩阵
    vec4：表示包含了4个浮点数的向量
    varying是用于从顶点着色器传递到片元着色器或FragmentShader传递到下一步的输出变量
    uMVPMatrix * aPosition：通过4*4的变换矩阵变换位置后，输出给gl_Position。gl_Position是顶点着色器内置的输出变量。gl_FragColor:是片元着色器内置的输出变量。
    * */

    //顶点着色器
    //gl_Position:是顶点着色器内置的输出变量
    private val vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 vMatrix;" +
                    "void main() {" +
                    " gl_Position = vMatrix * vPosition;" +
                    "}"

    //片元着色器
    //gl_FragColor:是片元着色器内置的输出变量
    //precision mediump float; 计算时float的精度。有三种精度：highp/mediump/lowp，默认是highp
    //这样做的好处就是：能帮助着色器程序提高运行效率，减少内存开支
    private val fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    " gl_FragColor = vColor;" +
                    "}"

    //圆坐标
    //将圆分成360个扇形，在将扇形近似到三角形
    private val triangleCoords: FloatArray

    private val colors = floatArrayOf(
            1.0f, 1.0f, 1.0f, 1.0f
    )

    private val COORDS_PRE_VERTEX = 3
    //顶点个数
    private val vertexCount: Int
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

    private var nested = false

    init {
        triangleCoords = createOvalPositions()
        vertexCount = triangleCoords.size / COORDS_PRE_VERTEX

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

    fun setMatrix(matrix: FloatArray) {
        mMVPMatrix = matrix
        nested = true
    }

    private fun createOvalPositions(): FloatArray {
        val n = 360
        val radius = 0.5
        val angSpan = 360F / n
        val list = mutableListOf(0F, 0F, 0F)
        var index = 0F
        while (index <= 360F) {
            list.add((radius * Math.sin(index * Math.PI / 180F)).toFloat())
            list.add((radius * Math.cos(index * Math.PI / 180F)).toFloat())
            list.add(0F)
            index += angSpan
        }
        return list.toFloatArray()
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
        if (!nested) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        }

        //将程序加入到OpenGLES20环境
        GLES20.glUseProgram(mProgram)

        //获取变换矩阵vMatrix成员句柄
        val mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0)

        //获取顶点着色器的vPosition成员句柄
        val mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        //启用三角形顶点句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        //stride: 如果数据连续存放，则为0或size*sizeof(type)
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PRE_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)

//            //获取顶点着色器的aColor成员句柄
//            val mVcHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
//            //启用句柄
//            GLES20.glEnableVertexAttribArray(mVcHandle)
//            GLES20.glVertexAttribPointer(mVcHandle, 4, GLES20.GL_FLOAT, false, 0, vertexColorBuffer)

        val mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, colors, 0)

        /*
            int GL_POINTS       //将传入的顶点坐标作为单独的点绘制
            int GL_LINES        //将传入的坐标作为单独线条绘制，ABCDEFG六个顶点，绘制AB、CD、EF三条线
            int GL_LINE_STRIP   //将传入的顶点作为折线绘制，ABCD四个顶点，绘制AB、BC、CD三条线
            int GL_LINE_LOOP    //将传入的顶点作为闭合折线绘制，ABCD四个顶点，绘制AB、BC、CD、DA四条线。
            int GL_TRIANGLES    //将传入的顶点作为单独的三角形绘制，ABCDEF绘制ABC,DEF两个三角形
            int GL_TRIANGLE_FAN    //将传入的顶点作为扇面绘制，ABCDEF绘制ABC、ACD、ADE、AEF四个三角形
            int GL_TRIANGLE_STRIP   //将传入的顶点作为三角条带绘制，ABCDEF绘制ABC,BCD,CDE,DEF四个三角形
        * */

        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount)

        //Disable三角形顶点句柄
//            GLES20.glDisableVertexAttribArray(mVcHandle)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("Triangle", "onSurfaceChanged")
        GLES20.glViewport(0, 0, width, height)

        val ratio = width / height.toFloat()
        //遵循右手坐标系
        //拇指、食指、其余指为x、y、z轴
        //透视投影矩阵
        //[ -ratio, ratio, -1F, 1F]指near面比例，far面遵从near面
        //near指camera到near面的距离
        //far指camera到far面的距离
        //6个参数设定了一个可见的视锥体，不在该范围内的就裁剪不显示
        //在视锥体内的物体最后会映射到near面？？
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1F, 1F, 3F, 7F)
        //相机位置矩阵
        //eye指相机坐标
        //center指目标的中心点,指相机是朝着center看，两个点构成一个视线。跟图元没有关系
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