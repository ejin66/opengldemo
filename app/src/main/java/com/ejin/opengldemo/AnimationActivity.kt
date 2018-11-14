package com.ejin.opengldemo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_demo_3.*

class AnimationActivity: AppCompatActivity() {

    companion object {
        fun start(activity: AppCompatActivity) {
            Intent(activity, AnimationActivity::class.java).let {
                activity.startActivity(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_3)
        val animRender = Animation(this)

        glView.setRenderer(animRender)

        animation.setOnClickListener {
            animRender.start(glView, "assets/cc.zip")
        }
    }

}