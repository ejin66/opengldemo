package com.ejin.opengldemo

import android.content.Context
import android.opengl.ETC1Util
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Animation(context: Context): GLSurfaceView.Renderer {

    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "attribute vec2 vCoord;" +
            "varying vec2 aCoord;" +
            "uniform mat4 vMatrix;" +
            "void main() {" +
            " aCoord = vCoord;" +
            " gl_Position = vPosition;" +
            "}"

    private val fragmentShaderCode = "precision mediump float;" +
            "varying vec2 aCoord;" +
            "uniform sampler2D vTexture;" +
            "uniform sampler2D vTextureAlpha;" +
            "void main() {" +
            " vec4 color = texture2D(vTexture, aCoord);" +
            " color.a = texture2D(vTextureAlpha, aCoord).a;" +
            " gl_FragColor = color;" +
            "}"

    private val vertexCoords = floatArrayOf(
            -1F, 1F,
            -1F, -1F,
            1F, 1F,
            1F, -1F
    )

    private val textureCoords = floatArrayOf(
            0F, 0F,
            0F, 1F,
            1F, 0F,
            1F, 1F
    )

    private val zipPkmReader = ZipPkmReader(context.assets)

    private var mProgram = -1
    private var positionHandler = -1
    private var coordHandler = -1
    private var matrixHandler = -1
    private var textureHandler = -1
    private var textureAlphaHandler = -1

    private val projectMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val finalMatrix = FloatArray(16)

    private var viewWidth = 1
    private var viewHeight = 1
    private var dataWidth = 1
    private var dateHeight = 1

    private val textures = IntArray(2)

    private var isRunning = false

    private var glSurfaceView: GLSurfaceView? = null

    private fun createProgram() {
        mProgram = Util.createProgram(Util.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode),
                Util.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode))
    }

    private fun getHandler() {
        positionHandler = GLES20.glGetAttribLocation(mProgram, "vPosition")
        coordHandler = GLES20.glGetAttribLocation(mProgram, "vCoord")
        matrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        textureHandler = GLES20.glGetUniformLocation(mProgram, "vTexture")
        textureAlphaHandler = GLES20.glGetUniformLocation(mProgram, "vTextureAlpha")
    }

    private fun calculateMatrix() {
        val viewRatio = viewWidth / viewHeight.toFloat()
        val dataRatio = dataWidth / dateHeight.toFloat()

        if (dataRatio > viewRatio) {
            Matrix.orthoM(projectMatrix, 0, -1F, 1F, -dataRatio / viewRatio, dataRatio / viewRatio, 3F, 7F)
        } else {
            Matrix.orthoM(projectMatrix, 0, -viewRatio / dataRatio, viewRatio / dataRatio, -1F, 1F, 3F, 7F)
        }

        Matrix.setLookAtM(viewMatrix, 0, 0F, 0F, 7F, 0F, 0F, 0F, 0F, 1F, 0F)
        Matrix.multiplyMM(finalMatrix, 0, projectMatrix, 0, viewMatrix, 0)
    }

    private fun createTextures() {
        GLES20.glGenTextures(2, textures, 0)
        textures.forEach {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, it)
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR.toFloat())
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE.toFloat())
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE.toFloat())
        }
    }

    fun start(view: GLSurfaceView, path: String) {
        if (isRunning) {
            Log.e(javaClass.simpleName, "animation is running")
            return
        }

        zipPkmReader.setZipPath(path)
        val isZipOpened = zipPkmReader.open()
        Log.d(javaClass.simpleName, "isZipOpened: $isZipOpened")
        if (isZipOpened) {
            view.requestRender()
            glSurfaceView = view
            isRunning = true
        }
    }

    fun stop() {
        glSurfaceView = null
        isRunning = false
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d(javaClass.simpleName, "onDrawFrame")
        val t = zipPkmReader.nextTexture ?:run {
            stop()
            return
        }
        val tAlpha = zipPkmReader.nextTexture ?:return

        dataWidth = t.width
        dateHeight = t.height
        calculateMatrix()

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUseProgram(mProgram)

        GLES20.glEnableVertexAttribArray(positionHandler)
        GLES20.glEnableVertexAttribArray(coordHandler)
        GLES20.glVertexAttribPointer(positionHandler, 2, GLES20.GL_FLOAT, false, 0, Util.getFloatBuffer(vertexCoords))
        GLES20.glVertexAttribPointer(coordHandler, 2, GLES20.GL_FLOAT, false, 0, Util.getFloatBuffer(textureCoords))

        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, finalMatrix, 0)

        //GL_TEXTURE_2D默认纹理单元是GLES20.GL_TEXTURE0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D, 0, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, t)
        //0表示GLES20.GL_TEXTURE0，1表示GLES20.GL_TEXTURE1, 以此类推
        GLES20.glUniform1i(textureHandler, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1])
        ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D, 0, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, tAlpha)
        GLES20.glUniform1i(textureAlphaHandler, 1)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCoords.size / 2)

        GLES20.glDisableVertexAttribArray(positionHandler)
        GLES20.glDisableVertexAttribArray(coordHandler)

        tryCatch({
            Thread.sleep(50)
        }, {
            it.printStackTrace()
        })
        glSurfaceView?.requestRender()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        viewWidth = width
        viewHeight = height
        calculateMatrix()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        createProgram()
        getHandler()
        createTextures()
        GLES20.glClearColor(0.5F, 0.5F, 0.5F, 1F)
    }
}