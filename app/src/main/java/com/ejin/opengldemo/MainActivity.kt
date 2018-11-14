package com.ejin.opengldemo

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import android.widget.Button
import com.ejin.opengldemo.camera.Camera2Tool
import com.ejin.opengldemo.camera.CameraActivity
import com.ejin.opengldemo.camera.CameraTextureViewActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val models = mutableListOf(
            Model(TYPE_TRIANGLE, "三角形"),
            Model(TYPE_TRIANGLE_1, "等腰三角形"),
            Model(TYPE_SQUARE, "正方形"),
            Model(TYPE_OVAL, "圆形"),
            Model(TYPE_CUBE, "立方体"),
            Model(TYPE_CONE, "圆锥"),
            Model(TYPE_CYLINDER, "圆柱"),
            Model(TYPE_IMAGE_TEXTURE, "图片纹理"),
            Model(TYPE_IMAGE_TEXTURE_GARY, "黑白图片"),
            Model(TYPE_IMAGE_TEXTURE_BIG, "图片放大"),
            Model(TYPE_IMAGE_TEXTURE_DIM, "图片模糊"),
            Model(TYPE_MOVE, "位移")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        models.forEach {
            val tmp = it
            val button = Button(baseContext)
            button.text = it.name
            button.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            button.setOnClickListener {
                Intent(baseContext, DemoActivity::class.java).apply {
                    putExtra("type", tmp.type)
                    startActivity(this)
                }
            }
            mainLayout.addView(button)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
        }



        camera.setOnClickListener {
            CameraActivity.start(this)
        }

        camera1.setOnClickListener {
            CameraActivity.start(this, 1)
        }

        camera2.setOnClickListener {
            CameraActivity.start(this, 2)
        }

        camera3.setOnClickListener {
            CameraActivity.start(this, 3)
        }

        animation.setOnClickListener {
            AnimationActivity.start(this)
        }

        Camera2Tool.test(this)
    }

    data class Model(var type: Int, var name: String)
}
