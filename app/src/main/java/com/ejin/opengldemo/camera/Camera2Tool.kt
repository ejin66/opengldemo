package com.ejin.opengldemo.camera

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.WindowManager
import com.ejin.opengldemo.tryCatch

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
object Camera2Tool {

    private var isCameraRunning = false
    private var mCamera: CameraDevice? = null
    private var mSession: CameraCaptureSession? = null
    private var mHandler: Handler? = null

    fun test(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        //一般前置摄像头的id是1, 相机默认旋转角度是270; 后置摄像头的id是0, 默认旋转角度是90
        cameraManager.cameraIdList.forEach {
            Log.d("Camera2Tool", it)
            cameraManager.getCameraCharacteristics(it).apply {
                //返回值说明
                //LEGACY < LIMITED < FULL < LEVEL_3. 越靠右边权限越大
                //INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED: 0 摄像头有使用限制，可能有一些或者没有“全支持”设备的特性
                //INFO_SUPPORTED_HARDWARE_LEVEL_FULL: 1 全方位的硬件支持，允许手动控制全高清的摄像。
                //INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY: 2
                //INFO_SUPPORTED_HARDWARE_LEVEL_3: 3
                get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                //获取支持的分辨率
                val configMap = get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                configMap.getOutputSizes(ImageFormat.JPEG).forEach {
                    Log.d(Camera2Tool.javaClass.simpleName, "preview: " + it.toString())
                }

                Log.d("Camera2Tool", "$it orientation: ${get(CameraCharacteristics.SENSOR_ORIENTATION)}")
            }
        }
    }

    fun openCamera(context: Context, surfaceTexture: SurfaceTexture, width: Int, height: Int, callback: (Int, Int) -> Unit) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (isCameraRunning) return

        isCameraRunning = true

        Log.d(Camera2Tool.javaClass.simpleName, "args: $width, $height")

        val handlerThread = HandlerThread("camera 2 thread")
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraCharacteristics =  cameraManager.getCameraCharacteristics(cameraManager.cameraIdList[0])

        val configMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val supportedSize = configMap.getOutputSizes(SurfaceTexture::class.java).apply {
            forEach {
                Log.d(Camera2Tool.javaClass.simpleName, "preview: " + it.toString())
            }
        }

        val windowRotation = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
        val sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)

        val selectedSize = selectOutputSize(supportedSize, width, height, windowRotation)

        var previewSize = selectedSize
        when (ScreenRotation.find(windowRotation)) {
            ScreenRotation.ROTATION_0, ScreenRotation.ROTATION_180 -> if (sensorOrientation == 90 || sensorOrientation == 270) {
                previewSize = Size(selectedSize.height, selectedSize.width)
            }
            ScreenRotation.ROTATION_90, ScreenRotation.ROTATION_270 -> if (sensorOrientation == 0 || sensorOrientation == 180) {
                previewSize = Size(selectedSize.height, selectedSize.width)
            }
        }

        Log.d(Camera2Tool.javaClass.simpleName, "original: $width x $height, preview size: $previewSize")
        callback.invoke(previewSize.height, previewSize.width)
        surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)

        cameraManager.openCamera(cameraManager.cameraIdList[0], object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice?) {
                Log.d(Camera2Tool.javaClass.simpleName, "camera opened")
                mCamera = camera
                camera?.let {
                    preview(it, surfaceTexture)
                }
            }

            override fun onDisconnected(camera: CameraDevice?) {
                Log.d(Camera2Tool.javaClass.simpleName, "camera disconnected")
                mCamera = camera
                clear()
            }

            override fun onError(camera: CameraDevice?, error: Int) {
                mCamera = camera
                Log.e(Camera2Tool.javaClass.simpleName, "open camera error: $error")
                clear()
            }
        }, mHandler)

    }

    fun stopCamera() {
        clear()
    }

    private fun selectOutputSize(outputSizes: Array<out Size>, width: Int, height: Int, windowRotation: Int): Size {
        val viewRatio = when (ScreenRotation.find(windowRotation)) {
            ScreenRotation.ROTATION_0, ScreenRotation.ROTATION_180 -> width / height.toFloat()
            else -> height / width.toFloat()
        }
        val offset = 0.2F
        return outputSizes.filter {
            (it.width / it.height.toFloat() > viewRatio * (1 - offset)
                ||  it.width / it.height.toFloat() < viewRatio * (1 + offset))
            && Math.max(it.width, it.height) <= Math.max(width, height)
        }
                .sortedBy { Math.max(it.width, it.height) }
                .lastOrNull() ?:Size(width, height)
    }

    private fun preview(camera: CameraDevice, surfaceTexture: SurfaceTexture) {
        Log.d(Camera2Tool.javaClass.simpleName, "start preview")

        val surface = Surface(surfaceTexture)
        camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
            set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            addTarget(surface)
        }.build().let {
            camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession?) {
                    mSession = session
                    Log.e(Camera2Tool.javaClass.simpleName, "create capture session configure failed")
                    clear()
                }

                override fun onConfigured(session: CameraCaptureSession?) {
                    mSession = session
                    tryCatch({
                        Log.d(Camera2Tool.javaClass.simpleName, "set repeat request")
                        session?.setRepeatingRequest(it, object : CameraCaptureSession.CaptureCallback() {

                        }, mHandler)
                    }, {
                        it.printStackTrace()
                        clear()
                    })
                }
            }, mHandler)
        }
    }

    private fun clear() {
        mCamera?.close()
        mSession?.close()
        mHandler?.looper?.quitSafely()

        mCamera = null
        mSession = null
        mHandler = null
        isCameraRunning = false
    }

    enum class ScreenRotation(var rotation: Int) {
        ROTATION_0(90),
        ROTATION_90(0),
        ROTATION_180(270),
        ROTATION_270(180);

        companion object {
            fun find(rotation: Int): ScreenRotation {
                return values().firstOrNull { it.rotation == rotation } ?: values()[0]
            }
        }

    }
}