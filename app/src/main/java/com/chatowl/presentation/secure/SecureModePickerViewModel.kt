package com.chatowl.presentation.secure

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.utils.SecureAppUtils

class SecureModePickerViewModel(
    application: Application,
    fragment: Fragment
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
            return SecureModePickerViewModel(application, fragment) as T
        }
    }

    val app = application
    val fragment = fragment

    val isFingerprintAvailable: LiveData<Boolean> get() = _isFingerprintAvailable
    private val _isFingerprintAvailable = MutableLiveData(false)

    init {
        checkBiometricSupport()
    }

    private fun checkBiometricSupport() {
        _isFingerprintAvailable.value = checkFingerprintSupport()
    }

    val executor = ContextCompat.getMainExecutor(fragment.requireContext())

    fun fingerprintPressed() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate!")
            .setSubtitle("Use biometric scanner to authenticate.")
            .setConfirmationRequired(false)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
            .setNegativeButtonText("Cancel")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    fun createCodePressed() {
        _navigate.value = SecureModePickerFragmentDirections.actionSecureModePickerFragmentToSecureCreatePinFragment()
    }

    fun skipPressed() {
        SecureAppUtils.cleanSecureMode()
        _navigate.value =
            SecureModePickerFragmentDirections.actionSecureModePickerFragmentToNavMain()
    }

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
                SecureAppUtils.cleanSecureMode()
                SecureAppUtils.setSaveSecureMode(SecureAppUtils.SecureMode.MODE_BIOMETRIC)
                _navigate.value =
                    SecureModePickerFragmentDirections.actionSecureModePickerFragmentToNavMain()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(
                    app, "Authentication failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    private fun notifyUser(message: String) {
        Toast.makeText(app, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkFingerprintSupport(): Boolean {
        val biometricManager = BiometricManager.from(app)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

}