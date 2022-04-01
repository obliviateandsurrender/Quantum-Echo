package com.raedghazal.nyuad_hackathon

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.raedghazal.nyuad_hackathon.databinding.FragmentCameraBinding
import java.util.*

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding: FragmentCameraBinding get() = _binding!!

    private var cameraSettings: CameraSettings? = null

    private val picturePickerContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            if (imageUri != null) {
                onPicturePickedFromGallery(imageUri)
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if (isGranted) {
                setupCamera()
            } else {
                context?.let {
                    MaterialDialog(it)
                        .title(R.string.require_camera_permission)
                        .positiveButton(R.string.close) {
                            findNavController().popBackStack()
                        }.show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            val cameraPermissionStatus =
                ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA)

            if (cameraPermissionStatus == PackageManager.PERMISSION_GRANTED) {
                setupCamera()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnImportPicture.setOnClickListener {
            picturePickerContract.launch("image/*")
        }

        binding.btnTakePicture.setOnClickListener {
            context?.let { context ->
                val fileName = "${UUID.randomUUID()}.jpg"
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
                    }
                }

                val outputOptions = ImageCapture.OutputFileOptions
                    .Builder(
                        context.contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                    .build()

                cameraSettings?.imageCapture?.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    onImageSaved()
                )
            }
        }
    }

    private fun setupCamera() {
        context?.let { context ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val processCameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                this.cameraSettings =
                    CameraSettings(processCameraProvider, binding.cameraPreview.display)

                val cameraSettings = cameraSettings ?: return@addListener
                cameraSettings.processCameraProvider.unbindAll()
                try {
                    cameraSettings.useNewConfigs { preview, imageCapture, cameraSelector ->
                        val camera = cameraSettings.processCameraProvider.bindToLifecycle(
                            this,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                        cameraSettings.camera = camera
                        preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                    }
                } catch (e: Exception) {

                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    private fun onImageSaved(): ImageCapture.OnImageSavedCallback {
        return object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let { imageUri ->
                    CameraFragmentDirections.actionCameraFragmentToPreviewPictureFragment(imageUri)
                        .navigate(this@CameraFragment)
                }
                cameraSettings?.unbindUseCases()
            }

            override fun onError(exc: ImageCaptureException) {
                context?.let { context ->
                    MaterialDialog(context)
                        .title(R.string.something_went_wrong)
                        .positiveButton(R.string.retry)
                        .show()
                }
            }
        }
    }

    private fun onPicturePickedFromGallery(imageUri: Uri) {
        CameraFragmentDirections.actionCameraFragmentToPreviewPictureFragment(imageUri).navigate(this)
    }
}