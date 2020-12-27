package com.example.dizzynks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.Nullable
import com.google.ar.sceneform.ux.ArFragment

class CustomArFragment : ArFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val frameLayout =
            super.onCreateView(inflater, container, savedInstanceState) as FrameLayout?
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        return frameLayout
    }
}