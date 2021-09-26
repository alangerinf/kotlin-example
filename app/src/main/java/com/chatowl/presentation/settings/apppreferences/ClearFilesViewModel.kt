package com.chatowl.presentation.settings.apppreferences

import android.app.Application
import androidx.lifecycle.LiveData
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.utils.FileUtils
import com.chatowl.presentation.common.utils.SingleLiveEvent

class ClearFilesViewModel(application: Application) : BaseViewModel(application) {

    private val fileUtils = FileUtils(application, "Downloads")

    val success: LiveData<Boolean> get() = _success
    private val _success = SingleLiveEvent<Boolean>()

    fun onConfirmClicked() {
        _success.value = fileUtils.mediaPath.deleteRecursively()
    }

}
