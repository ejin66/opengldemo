package com.ejin.opengldemo

import android.graphics.SurfaceTexture
import android.opengl.*
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class Camera: GLSurfaceView.Renderer {

    var surfaceTexture: SurfaceTexture? = null
    var dataWidth = 1F
    var dataHeight = 1F
    var viewWidth = 1F
    var viewHeight = 1F

    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "attribute vec2 vCoordinate;" +
            "uniform mat4 vMatrix;" +
            "varying vec2 aCoordinate;" +
            "void main() {" +
            " gl_Position = vMatrix * vPosition;" +
            " aCoordinate = vCoordinate;" +
            "}"

    //texture2D GSLS内置函数，用于2D纹理取样
    //samplerExternalOES 纹理采样器
    //要在头部增加使用扩展纹理的声明#extension GL_OES_EGL_image_external : require
    private val fragmentShaderCode = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "uniform samplerExternalOES vTexture;" +
            "varying vec2 aCoordinate;" +
            "uniform int vType;" +
            "float t(float f) {" +
            " if (f <= 0.0) { " +
            "   return max(abs(f), 0.01);" +
            " } else if (f >= 1.0){" +
            "  return min(2.0 - f, 0.99);" +
            " } else {" +
            "   return f;" +
            " }" +
            "}" +
            "vec2 amend(vec2 v) {" +
            " return vec2(t(v.x), t(v.y));" +
            "}" +
            "void main() {" +
            " vec4 nColor = texture2D(vTexture, aCoordinate);" +
            " if (vType == 1) {" +
            "  float c = nColor.r * 0.3 + nColor.g * 0.59 + nColor.g * 0.11;" +
            "  gl_FragColor = vec4(c, c, c, nColor.a);" +
            " } else if (vType == 2) {" +
            "  vec2 center = vec2(0.4, 0.4);" +
            "  float r1 = 0.25;" +
            "  float distance = distance(vec2(aCoordinate.x, aCoordinate.y), center);" +
            "  if (distance < r1) {" +
            "   gl_FragColor = texture2D(vTexture, vec2(aCoordinate.x/2.0 + center.x/2.0, aCoordinate.y/2.0 + center.y/2.0));" +
            "  } else { " +
            "   gl_FragColor = nColor;" +
            "  }" +
            " } else if (vType == 3) {" +
            "  float a1 = 0.01;" +
            "  nColor += texture2D(vTexture, amend(vec2(aCoordinate.x - a1, aCoordinate.y - a1)));" +
            "  nColor += texture2D(vTexture, amend(vec2(aCoordinate.x - a1, aCoordinate.y + a1)));" +
            "  nColor += texture2D(vTexture, amend(vec2(aCoordinate.x + a1, aCoordinate.y - a1)));" +
            "  nColor += texture2D(vTexture, amend(vec2(aCoordinate.x + a1, aCoordinate.y + a1)));" +
            "  nColor += texture2D(vTexture, amend(vec2(aCoordinate.x - a1, aCoordinate.y)));" +
            "  nColor += texture2D(vTexture, amend(vec2(aCoordinate.x + a1, aCoordinate.y)));" +
            "  nColor += texture2D(vTexture, amend(vec2(aCoordinate.x, aCoordinate.y - a1)));" +
            "  nColor += texture2D(vTexture, amend(vec2(aCoordinate.x, aCoordinate.y + a1)));" +
            "  gl_FragColor = nColor/9.0;" +
            " } else {" +
            "  gl_FragColor = nColor;" +
            " }" +
            "}"

    private val sPos = floatArrayOf(
            -1F, 1F,
            -1F, -1F,
            1F, 1F,
            1F, -1F
    )

    private val sCoord = floatArrayOf(
            0F, 0F,
            0F, 1F,
            1F, 0F,
            1F, 1F
    )

    private val projectMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val matrix = FloatArray(16)
    private var mProgram = -1
    private var positionHandler = -1
    private var coordinateHandler = -1
    private var matrixHandler = -1
    private var textureHandler = -1
    private var typeHandler = -1
    private var textureId = -1

    private var listener: SurfaceTexture.OnFrameAvailableListener? = null

    var type = 0

    fun setSize(dataWidth: Int, dataHeight: Int, viewWidth: Int, viewHeight: Int) {
        this.dataWidth = dataWidth.toFloat()
        this.dataHeight = dataHeight.toFloat()
        this.viewWidth = viewWidth.toFloat()
        this.viewHeight = viewHeight.toFloat()
        calculateMatrix()
    }

    private fun calculateMatrix() {
        val viewRatio = viewWidth / viewHeight
        val dataRatio = dataWidth / dataHeight

        if (viewRatio > dataRatio) {
            Matrix.orthoM(projectMatrix, 0, -1F, 1F, -dataRatio/viewRatio, dataRatio/viewRatio, 3F, 7F)
        } else {
            Matrix.orthoM(projectMatrix, 0, -viewRatio/dataRatio, viewRatio/dataRatio, -1F, 1F, 3F, 7F)
        }

        Matrix.setLookAtM(viewMatrix, 0, 0F, 0F, 7F, 0F, 0F, 0F, 0F, 1F, 0F)

        val moveMatrix = MatrixUtil.start()
                .rotate(270F)
                .getMatrix()

        Matrix.multiplyMM(moveMatrix, 0, viewMatrix, 0, moveMatrix, 0)
        Matrix.multiplyMM(matrix, 0, projectMatrix, 0, moveMatrix, 0)
    }

    fun setFrameAvailableListener(listener: SurfaceTexture.OnFrameAvailableListener) {
        this.listener = listener
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        surfaceTexture?.updateTexImage()

        GLES20.glUseProgram(mProgram)
        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, matrix, 0)

        GLES20.glUniform1i(typeHandler, type)

        GLES20.glEnableVertexAttribArray(positionHandler)
        GLES20.glEnableVertexAttribArray(coordinateHandler)

        GLES20.glVertexAttribPointer(positionHandler, 2, GLES20.GL_FLOAT, false, 0, Util.getFloatBuffer(sPos))
        GLES20.glVertexAttribPointer(coordinateHandler, 2, GLES20.GL_FLOAT, false, 0, Util.getFloatBuffer(sCoord))

        //代表纹理单元的索引，0/1/2...依次排下来的。即绑定到GLES20.GL_TEXTURE_2D纹理的索引
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        Log.d("Camera", "bind texture: $textureId")
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        Log.d("Camera", "bind texture end")
        GLES20.glUniform1i(textureHandler, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sPos.size / 2)

        GLES20.glDisableVertexAttribArray(positionHandler)
        GLES20.glDisableVertexAttribArray(coordinateHandler)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        viewWidth = width.toFloat()
        viewHeight = height.toFloat()
        Log.d("Camera", "$viewWidth / $viewHeight = ${viewWidth/viewHeight}")
        calculateMatrix()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mProgram = Util.createProgram(Util.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode),
                Util.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode))

        positionHandler = GLES20.glGetAttribLocation(mProgram, "vPosition")
        coordinateHandler = GLES20.glGetAttribLocation(mProgram, "vCoordinate")
        matrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        textureHandler = GLES20.glGetUniformLocation(mProgram, "vTexture")
        typeHandler = GLES20.glGetUniformLocation(mProgram, "vType")

        GLES20.glClearColor(0.5F, 0.5F, 0.5F, 1F)

        textureId = createTexture()
        surfaceTexture = SurfaceTexture(textureId).apply {
            setOnFrameAvailableListener(listener)
        }
    }

    //创建相机预览的texture。相机预览需要texture类型是GLES11Ext.GL_TEXTURE_EXTERNAL_OES
    private fun createTexture(): Int {
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat())
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR.toFloat())
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE.toFloat())
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE.toFloat())
        return texture[0]
    }
}