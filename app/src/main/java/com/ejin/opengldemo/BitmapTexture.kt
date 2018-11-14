package com.ejin.opengldemo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class BitmapTexture(var mContext: Context): GLSurfaceView.Renderer {

    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "attribute vec2 vCoordinate;" +
            "uniform mat4 vMatrix;" +
            "varying vec2 aCoordinate;" +
            "void main() {" +
            " gl_Position = vMatrix * vPosition;" +
            " aCoordinate = vCoordinate;" +
            "}"

    //texture2D GSLS内置函数，用于2D纹理取样
    //sampler2D 取样器类型，对应GL_TEXTURE_2D
    private val fragmentShaderCode = "precision mediump float;" +
            "uniform sampler2D vTexture;" +
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

    private val projectMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val matrix = FloatArray(16)
    private var mProgram = -1
    private var positionHandler = -1
    private var coordinateHandler = -1
    private var matrixHandler = -1
    private var textureHandler = -1
    private var typeHandler = -1
    private var mBitmap: Bitmap? = null

    var type = 0


    private val sPos = floatArrayOf(
            -1F, 1F,
            -1F, -1F,
            1F, 1F,
            1F, -1F
    )

    //不管图片的尺寸，最后会平铺在这个面积上
    private val sCoord = floatArrayOf(
            0F, 0F,
            0F, 1F,
            1F, 0F,
            1F, 1F
    )

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUseProgram(mProgram)
        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, matrix, 0)

        GLES20.glUniform1i(typeHandler, type)

        GLES20.glEnableVertexAttribArray(positionHandler)
        GLES20.glEnableVertexAttribArray(coordinateHandler)

        GLES20.glVertexAttribPointer(positionHandler, 2, GLES20.GL_FLOAT, false, 0, Util.getFloatBuffer(sPos))
        GLES20.glVertexAttribPointer(coordinateHandler, 2, GLES20.GL_FLOAT, false, 0, Util.getFloatBuffer(sCoord))

        createTexture()

        //代表纹理单元的索引，0/1/2...依次排下来的。即绑定到GLES20.GL_TEXTURE_2D纹理的索引
        GLES20.glUniform1i(textureHandler, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sPos.size / 2)

        GLES20.glDisableVertexAttribArray(positionHandler)
        GLES20.glDisableVertexAttribArray(coordinateHandler)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        if (mBitmap == null) return

        GLES20.glViewport(0, 0, width, height)

        val bitmapRatio = mBitmap!!.width / mBitmap!!.height.toFloat()
        val viewRatio = width / height.toFloat()

        if (bitmapRatio > viewRatio) {
            //y轴采集的长度，使得在屏幕上显示拉伸比例的时候，图片刚好宽高比一致。
            Matrix.orthoM(projectMatrix, 0, -1F, 1F, -bitmapRatio/viewRatio, bitmapRatio/viewRatio, 3F, 7F)
        } else {
            Matrix.orthoM(projectMatrix, 0, -viewRatio/bitmapRatio, viewRatio/bitmapRatio, -1F, 1F, 3F, 7F)
        }


        Matrix.setLookAtM(viewMatrix, 0, 0F, 0F, 7F, 0F, 0F, 0F, 0F, 1F, 0F)
        Matrix.multiplyMM(matrix, 0, projectMatrix, 0, viewMatrix, 0)
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
        GLES20.glEnable(GLES20.GL_TEXTURE_2D)
        getBitmap()
    }

    private fun getBitmap() {
        mBitmap = BitmapFactory.decodeResource(mContext.resources, R.mipmap.macat)
    }

    private fun createTexture(): Int {
        val texture = IntArray(1)
        if (mBitmap != null && !mBitmap!!.isRecycled) {
            //产生n个未使用过的纹理，并返回到texture[]
            GLES20.glGenTextures(1, texture, 0)
            //它告诉OpenGL下面对纹理的任何操作都是对它所绑定的纹理对象的
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0])
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR.toFloat())
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE.toFloat())
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)
            return texture[0]
        }
        return 0
    }
}