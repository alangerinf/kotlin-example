package com.chatowl.presentation.toolbox.mediaexercise

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.data.api.chatowl.ChatOwlAPIClient
import com.chatowl.data.entities.toolbox.PostRatePayload
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch

class RateViewModel(application: Application, val args: RateDialogFragmentArgs) :
    BaseViewModel(application), LifecycleObserver {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val args: RateDialogFragmentArgs) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RateViewModel(application, args) as T
        }
    }

    val currentState: LiveData<RateState> get() = _currentState
    private val _currentState = MutableLiveData(RateState.STATUS_SUBMIT)

    val stars: LiveData<Int> get() = _stars
    private val _stars = MutableLiveData(3)

    fun selectStars(rate: Int) {
        _stars.value = rate
    }

    fun pressSubmit(){
        if (stars.value!! <= 3) {
            _currentState.value = RateState.STATUS_SEND_FEEDBACK
        } else {
            pressSendRate()
        }
    }

    fun pressSendRate(feedback: String? = null) {
        viewModelScope.fetch({
            ChatOwlAPIClient.postRateExercise(
                PostRatePayload(
                    args.id,
                    _stars.value?:0,
                    feedback
                )
            )
        }, {
            _currentState.value = RateState.STATUS_THANKS
        }, {
            _currentState.value = RateState.STATUS_THANKS
        })

    }

    fun pressDontAskMeAgain() {
        _currentState.value = RateState.STATUS_NO_PROBLEM
        //save it locally
    }

    enum class RateState {
        STATUS_SUBMIT, // initial state
        STATUS_SEND_FEEDBACK,
        STATUS_THANKS,
        STATUS_NO_PROBLEM
    }

}
