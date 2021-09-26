package com.chatowl.presentation.apptour

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.data.entities.apptour.AppTourData
import com.chatowl.data.repositories.ChatOwlHomeRepository
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.home.HomeViewModel
import com.chatowl.presentation.settings.SettingsFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AppTourViewModel(application: Application,
                       private val userRepository: ChatOwlUserRepository,
                       private val args: AppTourFragmentArgs
) : BaseViewModel(application), LifecycleObserver {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val chatOwlUserRepository: ChatOwlUserRepository,
        private val appTourFragmentArgs: AppTourFragmentArgs
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AppTourViewModel(application, chatOwlUserRepository, appTourFragmentArgs) as T
        }
    }

    val goBack: LiveData<Boolean> get() = _goBack
    private val _goBack = MutableLiveData<Boolean>()

    val appTourData: LiveData<AppTourData> get() = _appTourData
    private val _appTourData = MutableLiveData<AppTourData>()

    fun onContinueClicked(){
     viewModelScope.fetch({
                          userRepository.getAppTour()
     },{
       _appTourData.value = it
     },{
         //TODO display error
     })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        _appTourData.value = args.appTourData
    }

    fun tourCompleted(){
        _goBack.value = true
    }


}