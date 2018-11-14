package com.ejin.opengldemo

import android.opengl.Matrix

object MatrixUtil {

    private var currentMatrix = FloatArray(16)

    fun start(): MatrixUtil{
        currentMatrix = floatArrayOf(
                1F, 0F, 0F, 0F,
                0F, 1F, 0F, 0F,
                0F, 0F, 1F, 0F,
                0F, 0F, 0F, 1F
        )
        return this
    }

    fun rotate(r: Float): MatrixUtil {
        Matrix.rotateM(currentMatrix, 0, r, 0F, 0F, 1F)
        return this
    }

    fun translate(x: Float, y: Float, z: Float): MatrixUtil {
        Matrix.translateM(currentMatrix, 0, x, y, z)
        return this
    }

    fun scale(s: Float): MatrixUtil {
        Matrix.scaleM(currentMatrix, 0, s, s, s)
        return this
    }

    fun getMatrix() = currentMatrix
}