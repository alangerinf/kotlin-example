package com.chatowl.presentation.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.presentation.common.fragments.ViewModelFragment

class IndexFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_index, container, false)
    }

    val looper = Handler(Looper.getMainLooper())
    val runnable = Runnable {
        findNavController().navigate(IndexFragmentDirections.actionIndexFragmentToSplashFragment())
    }

    override fun onResume() {
        super.onResume()
        looper.postDelayed(runnable,200)
    }

    override fun onDestroy() {
        looper.removeCallbacks(runnable)
        super.onDestroy()
    }

}