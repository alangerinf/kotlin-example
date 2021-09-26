package com.chatowl.presentation.secure

import android.app.Application
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.utils.SecureAppUtils

class SecureCodeAuthenticationViewModel(
    application: Application,
    val fragment: Fragment
) : BaseViewModel(application),
    LifecycleObserver {

    val TAG = this::class.java.simpleName

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val fragment: Fragment
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SecureCodeAuthenticationViewModel(application, fragment) as T
        }
    }


    val app = application
    val executor = ContextCompat.getMainExecutor(fragment.requireContext())


    val biometricPrompt = BiometricPrompt(fragment, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)
                Log.e(TAG, "Authentication error: $errString")
            }

            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(result)
                _scannerStatus.value = Pair(ScannerStatusType.SUCCESS,"Biometric scanning successful!")
                _navigate.value =
                    SecureCodeAuthenticationFragmentDirections.actionSecureCodeAuthenticationFragmentToNavMain()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                _scannerStatus.value = Pair(ScannerStatusType.ERROR,"Biometric scanning failed")
            }
        })

    fun iCannotAuthenticateMyself() {
        _navigate.value =
            SecureCodeAuthenticationFragmentDirections.actionSecureCodeAuthenticationFragmentToLogoutDialog()
    }

    val authenticationModes: LiveData<SecureAppUtils.SecureMode> get() = _authenticationModes
    private val _authenticationModes = MutableLiveData(SecureAppUtils.SecureMode.NONE)

    val scannerStatus: LiveData<Pair<ScannerStatusType, String>> get() = _scannerStatus
    private val _scannerStatus =
        MutableLiveData(Pair(ScannerStatusType.NORMAL,fragment.getString(R.string.tap_the_icon_to_start_scanning)))

    enum class ScannerStatusType{
        NORMAL,
        ERROR,
        SUCCESS,
    }

    var PIN = ""

    fun checkAuthenticationMode() {
        val (mode, code) = SecureAppUtils.getCurrentSecureMode()
        PIN = code
        _authenticationModes.value = mode
    }

    fun tryBiometricAuthentication() {
        _scannerStatus.value = Pair(ScannerStatusType.NORMAL,fragment.getString(R.string.tap_the_icon_to_start_scanning))
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate!")
            .setSubtitle("Use biometric scanner to authenticate.")
            .setConfirmationRequired(false)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .setNegativeButtonText("Cancel")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    fun tryPINAuthentication(code: String) {
        if (PIN == code) {
            _scannerStatus.value = Pair(ScannerStatusType.SUCCESS,app.getString(R.string.secure_app_success))
            _navigate.value =
                SecureCodeAuthenticationFragmentDirections.actionSecureCodeAuthenticationFragmentToNavMain()
        } else {
            _scannerStatus.value = Pair(ScannerStatusType.ERROR,app.getString(R.string.secure_app_doesnt_match))
        }
    }
}