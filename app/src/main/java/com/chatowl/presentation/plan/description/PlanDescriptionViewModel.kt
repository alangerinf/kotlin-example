package com.chatowl.presentation.plan.description

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chatowl.data.entities.plan.FullProgramDescription
import com.chatowl.data.entities.toolbox.fullscreen.FullscreenPlayerData
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.utils.SingleLiveEvent


class PlanDescriptionViewModel(application: Application, args: PlanDescriptionFragmentArgs) : BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val args: PlanDescriptionFragmentArgs) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PlanDescriptionViewModel(application, args) as T
        }
    }

    val programDescription: LiveData<FullProgramDescription> get() = _programDescription
    private val _programDescription = MutableLiveData<FullProgramDescription>()

    val fullscreenPlayer: LiveData<FullscreenPlayerData> get() = _fullscreenPlayer
    private val _fullscreenPlayer = SingleLiveEvent<FullscreenPlayerData>()

    init {
        _programDescription.value = args.programDescription
    }

    fun onPlayClicked() {
        programDescription.value?.let {
            _fullscreenPlayer.value = FullscreenPlayerData(
                name = "${it.name} ${it.tagLine}",
                backgroundUrl = it.thumbnailUrl,
                mediaUri = Uri.parse(it.videoUrl)
            )
        }
    }
}
