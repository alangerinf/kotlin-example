package com.chatowl.presentation.main

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.settings.apppreferences.AppPreferencesFragmentDirections


class MainViewModel(application: Application) : BaseViewModel(application) {

    private lateinit var connectivityManager: ConnectivityManager
    val isKeyboardOpen: LiveData<Boolean> get() = _isKeyboardOpen
    private val _isKeyboardOpen = MutableLiveData<Boolean>()

    val isNetworkAvailable: LiveData<Boolean> get() = _isNetworkAvailable
    private val _isNetworkAvailable = MutableLiveData<Boolean>()

    val isChatMuted: LiveData<Boolean> get() = _isChatMuted
    private val _isChatMuted = MutableLiveData(false)

    fun keyboardStatusChanged(newState: Boolean) {
        _isKeyboardOpen.value = newState
    }

    fun setNavigation (direction: NavDirections) {
        _navigate.value = direction
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isNetworkAvailable.postValue(true)
        }

        override fun onLost(network: Network) {
            _isNetworkAvailable.postValue(false)
        }
    }

    init {
        connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val builder = NetworkRequest.Builder()
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
        }

    }

    fun connectivityCheckRequested() {
        _isNetworkAvailable.value = isNetworkConnected()
    }

    private fun isNetworkConnected(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return connectivityManager.activeNetwork != null
        } else {
            return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
        }
    }

    fun setChatMuted(isMuted: Boolean) {
        _isChatMuted.value = isMuted
    }


}
