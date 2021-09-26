package com.chatowl.presentation.toolbox.exerciselocked

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.entities.toolbox.ExerciseLockedMessage
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.utils.SingleLiveEvent

class ExerciseLockedDialogViewModel(application: Application, args: ExerciseLockedDialogFragmentArgs) : BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val application: Application,
        val args: ExerciseLockedDialogFragmentArgs
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ExerciseLockedDialogViewModel(application, args) as T
        }
    }

    val exerciseLockedMessage: LiveData<ExerciseLockedMessage> get() = _exerciseLockedMessage
    private val _exerciseLockedMessage = MutableLiveData<ExerciseLockedMessage>()

    val dismiss: LiveData<Boolean> get() = _dismiss
    private val _dismiss = SingleLiveEvent<Boolean>()

    init {
        _exerciseLockedMessage.value = args.message
    }

    val action = Transformations.map(exerciseLockedMessage) {
        if(it.isBlockedBySubscription) {
            application.getString(R.string.subscribe)
        } else {
            application.getString(R.string.ok)
        }
    }

    val cancel = Transformations.map(exerciseLockedMessage) {
        if(it.isBlockedBySubscription) {
            application.getString(R.string.no_thanks)
        } else {
            application.getString(R.string.show_therapy_plan)
        }
    }

    fun onPrimaryActionClicked() {
        if(exerciseLockedMessage.value?.isBlockedBySubscription == true) {
            // Take to subscription screen
        } else {
            _dismiss.value = true
        }
    }

    fun onSecondaryActionClicked() {
        if(exerciseLockedMessage.value?.isBlockedBySubscription == true) {
            _dismiss.value = true
        } else {
            _navigate.value = ExerciseLockedDialogFragmentDirections.actionGlobalCurrentPlanFragment()
            _dismiss.value = true
        }
    }

}
