package com.ejin.opengldemo.camera

import android.content.Intent
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ejin.opengldemo.Camera
import com.ejin.opengldemo.R
import kotlinx.android.synthetic.main.activity_demo_1.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraActivity : AppCompatActivity() {

    companion object {
        fun start(activity: AppCompatActivity, type: Int) {
            Intent(activity, CameraActivity::class.java).let {
                it.putExtra("type", type)
                activity.startActivity(it)
            }
        }

        fun start(activity: AppCompatActivity) {
            start(activity, 0)
        }
    }

    val mCameraRender = getCameraRender()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_1)
        glView.setEGLContextClientVersion(2)
        glView.setRenderer(mCameraRender)
        glView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        val type = intent.getIntExtra("type", 0)
        (mCameraRender as Camera).type = type
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }

    private fun getCameraRender(): GLSurfaceView.Renderer {
        val render = object : Camera() {
            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                super.onSurfaceCreated(gl, config)
                Camera2Tool.openCamera(this@CameraActivity, surfaceTexture!!, glView.width, glView.height, { width, height ->
                    (mCameraRender as Camera).setSize(width, height, glView.width, glView.height)
                })
            }
        }
        render.setFrameAvailableListener(SurfaceTexture.OnFrameAvailableListener {
            glView.requestRender()
        })
        return render
    }

    override fun onDestroy() {
        super.onDestroy()
        Camera2Tool.stopCamera()
    }
}