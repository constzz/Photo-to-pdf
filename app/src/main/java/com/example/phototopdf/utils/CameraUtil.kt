package com.example.phototopdf.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraUtil(private val activity: AppCompatActivity) {
    private val activityContext = activity.baseContext

    //Camera
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var flashlightIsEnabled = false
    private var flashlightIsOn = false

    fun initCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                activityContext, it) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                    activity as Activity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    @SuppressLint("RestrictedApi")
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activityContext)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(activity.preview.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        activity, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

            flashlightIsEnabled = preview.camera?.cameraInfo?.hasFlashUnit() ?: false
        }, ContextCompat.getMainExecutor(activity.baseContext))

    }

    fun flashlightIsOn(): Boolean {
        return flashlightIsOn
    }

    fun flashlightIsEnabled(): Boolean {
        return flashlightIsEnabled
    }

    fun toggleFlashlight() {
        if (flashlightIsEnabled) {
            flashlightIsOn = !flashlightIsOn
        } else Log.ERROR
    }

    fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
        imageCapture.flashMode = if(flashlightIsOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        val photoFile = File(
                activityContext.cacheDir,
                SimpleDateFormat(FILENAME_FORMAT, Locale.US
                ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(activityContext), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                val msg = "Photo capture succeeded: $savedUri"
                Log.d(TAG, msg)
            }
        })
    }

    fun shutdownCamera() {
        cameraExecutor.shutdown()
    }

    companion object {
        const val TAG = "CameraUtil"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

}
