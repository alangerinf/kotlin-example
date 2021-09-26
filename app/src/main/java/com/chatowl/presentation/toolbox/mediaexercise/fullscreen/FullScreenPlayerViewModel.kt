package com.chatowl.presentation.toolbox.mediaexercise.fullscreen

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.*
import com.chatowl.data.entities.toolbox.fullscreen.FullscreenPlayerData
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.toolbox.mediaexercise.FullscreenPlayerContract.Companion.FULLSCREEN_PLAYER_DATA_KEY

class FullScreenPlayerViewModel(application: Application, extras: Bundle?) : BaseViewModel(application) {

    class Factory(private val application: Application, private val extras: Bundle?) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FullScreenPlayerViewModel(application, extras) as T
        }
    }

    private val fullscreenPlayerData: LiveData<FullscreenPlayerData> get() = _fullscreenExercise
    private val _fullscreenExercise = MutableLiveData<FullscreenPlayerData>()

    var startPosition = 0L
        private set

    init {
        _fullscreenExercise.value = extras?.getParcelable(FULLSCREEN_PLAYER_DATA_KEY)
        startPosition = fullscreenPlayerData.value?.position?.times(1000) ?: 0
    }

    val title = Transformations.map(fullscreenPlayerData) { exercise ->
        exercise.name
    }

    val mediaUri = Transformations.map(fullscreenPlayerData) { exercise ->
        exercise.mediaUri
    }

    val backgroundUrl = Transformations.map(fullscreenPlayerData) { exercise ->
        exercise.backgroundUrl
    }

    fun resetStartPosition() {
        startPosition = 0
    }

    val fixedLandscape = Transformations.map(fullscreenPlayerData) { exercise ->
        exercise.fixedLandscape
    }

}
