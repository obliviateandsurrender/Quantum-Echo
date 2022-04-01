package com.raedghazal.nyuad_hackathon

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.raedghazal.nyuad_hackathon.databinding.FragmentResultBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding: FragmentResultBinding get() = _binding!!

    private val args by navArgs<ResultFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pbLoading.show()
        binding.imgOriginalImage.setImageURI(args.imageUri)

        getTextFromImage()

        binding.btnConvertNext.setOnClickListener {
            ResultFragmentDirections.actionResultFragmentToHomeFragment().navigate(this)
        }
    }

    private fun getTextFromImage() {
        val context = context ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val networkResponse = AppRepository.getTextFromImage(args.imageUri, context)
            withContext(Dispatchers.Main) {
                binding.pbLoading.hide()
                networkResponse?.subscribe(
                    {
                        binding.tvConvertedText.text = it.body()?.result ?: ""
                    }, {
                        binding.tvConvertedText.text = getString(R.string.something_went_wrong)
                    })
            }
        }
    }

}