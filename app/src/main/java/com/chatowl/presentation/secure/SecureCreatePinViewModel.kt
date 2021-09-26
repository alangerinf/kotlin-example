package com.chatowl.presentation.secure

import android.app.Application
import android.app.DownloadManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.utils.SecureAppUtils

class SecureCreatePinViewModel(
    application: Application,
    fragment: Fragment
) : BaseViewModel(application),
    LifecycleObserver {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val fragment: Fragment
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SecureCreatePinViewModel(application, fragment) as T
        }
    }

    val app = application
    val fragment = fragment
    val currentCodeStatus: LiveData<InsertCodeStatus> get() = _currentCodeStatus
    private val _currentCodeStatus = MutableLiveData(InsertCodeStatus.FIRST_CODE)

    val errorMessage: LiveData<String> get() = _errorMessage
    private val _errorMessage = MutableLiveData("")

    val successMessage: LiveData<String> get() = _successMessage
    private val _successMessage = MutableLiveData("")

    private lateinit var firstCode: String

    fun changeToSecondCode(code: String) {
        firstCode = code
        _currentCodeStatus.value = InsertCodeStatus.SECOND_CODE
    }

    fun verifyCode(code: String) {
        if (firstCode == code) {
            SecureAppUtils.cleanSecureMode()
            SecureAppUtils.setSaveSecureMode(SecureAppUtils.SecureMode.MODE_PIN, code)
            _successMessage.value = app.getString(R.string.secure_app_success)
           _navigate.value = SecureCreatePinFragmentDirections.actionSecureCreatePinFragmentToNavMain()
        } else {
            _errorMessage.value = app.getString(R.string.secure_app_doesnt_match)
        }
    }

    fun changeToFirstCode(){
        firstCode = ""
        _currentCodeStatus.value = InsertCodeStatus.FIRST_CODE
    }

    enum class InsertCodeStatus(val subtitleMessageId: Int){
        FIRST_CODE(R.string.secure_app_insert_four_digits),
        SECOND_CODE(R.string.secure_app_repeat_code)
    }


    fun backPressed() {
        if (currentCodeStatus.value == InsertCodeStatus.FIRST_CODE) {
            fragment.findNavController().navigateUp()
        } else {
            changeToFirstCode()
        }

    }


}