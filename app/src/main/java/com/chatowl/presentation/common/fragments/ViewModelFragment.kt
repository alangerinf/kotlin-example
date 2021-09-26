package com.chatowl.presentation.common.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.navGraphViewModels
import com.chatowl.R
import com.chatowl.data.entities.tracking.PostEventTrackingRequest
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.activities.BaseActivity
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.utils.PreferenceHelper
import com.chatowl.presentation.common.utils.get
import com.chatowl.presentation.main.MainGraphViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

abstract class ViewModelFragment<VM : ViewModel>(private val modelClass: Class<VM>) :
    FragmentBase() {

    open fun getScreenName() = this::class.java.simpleName
    open fun getToolboxCategoryName() = ""
    open fun getTherapyPlanName() = ""
    open fun getExerciseName() = ""

    abstract fun addObservers()

    abstract fun removeObservers()

    protected lateinit var viewModel: VM

    val mainGraphViewModel: MainGraphViewModel by navGraphViewModels(R.id.nav_splash) {
        val application = ChatOwlApplication.get()
        val userPool = application.userPool
        MainGraphViewModel.Factory(application, userPool, requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = setViewModel()
        addObservers()
    }

    open fun setViewModel(): VM {
        return ViewModelProvider(this).get(modelClass)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeObservers()
    }

    protected fun closeKeyboard(v: View?) {
        (requireActivity() as? BaseActivity)?.closeKeyboard(v)
    }

    protected fun openKeyboard(v: View?) {
        (requireActivity() as? BaseActivity)?.openKeyboard(v)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {

        } catch (e: Exception) {

        }
        Log.i("ViewModelFragment", "onAttach() " + getScreenName())
        val accessToken = PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.ACCESS_TOKEN, "") ?: ""
        if (accessToken.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).fetch({
                ChatOwlUserRepository.sendTracking(
                    PostEventTrackingRequest.ViewedScreenBuilder()
                        .screenName(getScreenName())
                        .toolboxCategoryName(getToolboxCategoryName())
                        .therapyPlanName(getTherapyPlanName())
                        .exerciseName(getExerciseName())
                        .build())
            }, {
                Log.d("ViewModelFragment", getScreenName()+" done")
            }, {
                Log.e("ViewModelFragment", it.toString())
            })
        }

    }
}