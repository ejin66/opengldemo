package com.ejin.opengldemo.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

/**
 * Created by j17420 on 2018/4/23.
 */
class GLView(context: Context, attrs: AttributeSet): GLSurfaceView(context, attrs) {

    init {
        setEGLContextClientVersion(2)
    }

    override fun setRenderer(renderer: Renderer?) {
        super.setRenderer(renderer)
        //GLSurfaceView.RENDERMODE_CONTINUOUSLY 不间断的绘制,默认渲染模式是这种
        //GLSurfaceView.RENDERMODE_WHEN_DIRTY  在屏幕变脏时绘制,也就是当调用GLSurfaceView的requestRender ()方法后才会执行一次(第一次运行的时候会自动绘制一次)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

}