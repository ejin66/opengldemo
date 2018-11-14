package com.ejin.opengldemo

import android.opengl.GLES20
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Util {

    companion object {

        fun getFloatBuffer(array: FloatArray): FloatBuffer {
            return ByteBuffer.allocateDirect(array.size * 4).let {
                it.order(ByteOrder.nativeOrder())
                it.asFloatBuffer()
            }.apply {
                put(array)
                position(0)
            }
        }

        fun getShortBuffer(array: ShortArray): ShortBuffer {
            return ByteBuffer.allocateDirect(array.size * 2).let {
                it.order(ByteOrder.nativeOrder())
                it.asShortBuffer()
            }.apply {
                put(array)
                position(0)
            }
        }

        fun loadShader(type: Int, sourceCode: String): Int {
            Log.d("Util", "load shader $type")
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, sourceCode)
            GLES20.glCompileShader(shader)
            return shader
        }

        fun createProgram(shader1: Int, shader2: Int): Int {
            Log.d("Util", "createProgram")
            val program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, shader1)
            GLES20.glAttachShader(program, shader2)
            GLES20.glLinkProgram(program)
            return program
        }
    }

}