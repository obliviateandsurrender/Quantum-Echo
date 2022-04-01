package com.raedghazal.nyuad_hackathon

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.raedghazal.nyuad_hackathon.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding get() = _binding!!

    private val picturePickerContract = registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
        if (imageUri != null) {
            onPicturePickedFromGallery(imageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnTakePicture.setOnClickListener {
            HomeFragmentDirections.actionHomeFragmentToCameraFragment().navigate(this)
        }

        binding.btnImportPicture.setOnClickListener {
            picturePickerContract.launch("image/*")
        }
    }

    private fun onPicturePickedFromGallery(imageUri: Uri) {
        HomeFragmentDirections.actionHomeFragmentToPreviewPictureFragment(imageUri).navigate(this)
    }

}