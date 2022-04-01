package com.raedghazal.nyuad_hackathon

import android.util.DisplayMetrics
import android.util.Size
import android.view.Display
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider

class CameraSettings(
    val processCameraProvider: ProcessCameraProvider,
    private val display: Display
) {

    var camera: Camera? = null

    var imageCapture: ImageCapture? = null

    fun useNewConfigs(configs: (Preview, ImageCapture, CameraSelector) -> Unit) {
        val preview = getNewPreview()
        val imageCapture = getNewImageCapture().also { this.imageCapture = it }

        configs(preview, imageCapture, cameraSelector)
        this.imageCapture?.flashMode =
            if (hasFlash) ImageCapture.FLASH_MODE_AUTO else ImageCapture.FLASH_MODE_OFF
    }

    fun unbindUseCases() {
        camera = null
        processCameraProvider.unbindAll()
    }

    private val cameraSelector: CameraSelector
        get() {
            return CameraSelector
                .Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
        }

    private val hasFlash: Boolean get() = camera?.cameraInfo?.hasFlashUnit() ?: false

    private val targetResolution: Size = run {
        val metrics = DisplayMetrics().also {
            display.getMetrics(it)
        }
        Size(metrics.widthPixels, metrics.heightPixels)
    }

    private val targetRotation = display.rotation

    private fun getNewPreview(): Preview {
        return Preview.Builder().setTargetRotation(display.rotation).build()
    }

    private fun getNewImageCapture(): ImageCapture {
        return ImageCapture.Builder()
            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(targetRotation)
            .setTargetResolution(targetResolution)
            .build()
    }
}
