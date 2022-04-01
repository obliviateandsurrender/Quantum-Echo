package com.raedghazal.nyuad_hackathon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.raedghazal.nyuad_hackathon.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}