package com.raedghazal.nyuad_hackathon

import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController

fun NavDirections.navigate(fragment: Fragment) {
    findNavController(fragment).navigate(this)
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}