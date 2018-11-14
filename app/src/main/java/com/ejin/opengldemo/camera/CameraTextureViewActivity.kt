package com.ejin.opengldemo.camera

import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.TextureView
import com.ejin.opengldemo.R
import kotlinx.android.synthetic.main.activity_demo_2.*

class CameraTextureViewActivity: AppCompatActivity() {


    companion object {
        fun start(activity: AppCompatActivity) {
            Intent(activity, CameraTextureViewActivity::class.java).let {
                activity.startActivity(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_2)

        textureView.surfaceTextureListener = object: TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                return true
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
//                surface?.setOnFrameAvailableListener {
//                    Log.d("CameraTexture", "on frame available")
//                }
                Camera2Tool.openCamera(this@CameraTextureViewActivity, surface!!, width, height, { _,_ ->

                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Camera2Tool.stopCamera()
    }
}