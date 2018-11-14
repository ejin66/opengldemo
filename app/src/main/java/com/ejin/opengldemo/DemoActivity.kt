package com.ejin.opengldemo

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_demo_1.*

class DemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_1)
        val type = intent.getIntExtra("type", TYPE_DEFAULT)
        glView.setEGLContextClientVersion(2)
        glView.setRenderer(getRender(type))
        glView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
//        glView.requestRender()
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }

    private fun getRender(type: Int): GLSurfaceView.Renderer {
        return when (type) {
            TYPE_TRIANGLE -> Triangle()
            TYPE_TRIANGLE_1 -> Triangle2()
            TYPE_SQUARE -> Square()
            TYPE_OVAL -> Oval()
            TYPE_CUBE -> Cube()
            TYPE_CONE -> Cone()
            TYPE_CYLINDER -> Cylinder()
            TYPE_IMAGE_TEXTURE -> BitmapTexture(this)
            TYPE_IMAGE_TEXTURE_GARY -> BitmapTexture(this).apply {
                this.type = 1
            }
            TYPE_IMAGE_TEXTURE_BIG -> BitmapTexture(this).apply {
                this.type = 2
            }
            TYPE_IMAGE_TEXTURE_DIM -> BitmapTexture(this).apply {
                this.type = 3
            }
            TYPE_MOVE -> Move()
            else -> Cone()
        }
    }

}