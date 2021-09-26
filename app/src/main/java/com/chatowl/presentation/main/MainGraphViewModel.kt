package com.chatowl.presentation.main

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.navigation.NavDirections
import androidx.savedstate.SavedStateRegistryOwner
import com.amazonaws.mobileconnectors.cognitoidentityprovider.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler
import com.amazonaws.services.cognitoidentityprovider.model.SignUpResult
import com.amazonaws.services.cognitoidentityprovider.model.UsernameExistsException
import com.chatowl.R
import com.chatowl.data.api.chat.SocketManager
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.entities.tracking.PostEventTrackingRequest
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.extensions.isTestAddress
import com.chatowl.presentation.common.utils.*
import com.chatowl.presentation.common.utils.PreferenceHelper.Key.*
import com.chatowl.presentation.settings.logout.LogoutDialogFragmentDirections
import com.chatowl.presentation.signup.SignUpFragmentDirections
import com.chatowl.presentation.splash.SplashFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainGraphViewModel(
    application: Application,
    private val state: SavedStateHandle,
    private val userPool: CognitoUserPool
) : AndroidViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val userPool: CognitoUserPool,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return MainGraphViewModel(application, handle, userPool) as T
        }
    }

    val navigate: LiveData<NavDirections> get() = _navigate
    private val _navigate = SingleLiveEvent<NavDirections>()

    val challengeContinuation: LiveData<ChallengeContinuation> get() = _challengeContinuation
    private val _challengeContinuation = SingleLiveEvent<ChallengeContinuation>()

    val loginSuccess: LiveData<Boolean> get() = _loginSuccess
    private val _loginSuccess = SingleLiveEvent<Boolean>()

    val errorMessage: LiveData<Int> get() = _errorMessage
    private val _errorMessage = SingleLiveEvent<Int>()

    private val _accSentCodes = MutableLiveData<Int>(0)
    val accSentCodes: LiveData<Int>
        get() = _accSentCodes

    fun resetAccSentCodes() {
        _accSentCodes.value = 0
    }

    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _isLoading = MutableLiveData<Boolean>()

    val email = state.getLiveData<String?>(ATTRIBUTE_EMAIL)

    companion object {
        const val ATTRIBUTE_NICKNAME = "nickname"
        const val ATTRIBUTE_EMAIL = "email"

        const val AUTHENTICATION_PARAMETER_USERNAME = "USERNAME"
        const val CUSTOM_CHALLENGE = "CUSTOM_CHALLENGE"
        const val CHALLENGE_ANSWER = "ANSWER"

        const val DUMMY_PASSWORD = "K6NXtsVpKu7JDmtX7neWDHmT38LPh3Mt"
        const val TAG = "COGNITO"
    }

    fun initAuth(userId: String? = null) {
        when {
            userId != null -> {
                PreferenceHelper.getChatOwlPreferences().set(IS_TESTER, userId.isTestAddress())
                _isLoading.value = true
                userPool.getUser(userId).getSessionInBackground(authenticationHandler)
            }
            else -> {
                val currentUser: CognitoUser = userPool.currentUser
                val accessToken = PreferenceHelper.getChatOwlPreferences().get(
                    ACCESS_TOKEN, ""
                ) ?: ""
                if (currentUser.userId != null && accessToken.isNotEmpty()) {
                    _isLoading.value = true
                    currentUser.getSessionInBackground(authenticationHandler)
                } else {
                    _navigate.value = SplashFragmentDirections.actionSplashToOnboarding()
                }
            }
        }
    }

    fun onSendCodeClick(email: String) {
        currentEmail = email
        PreferenceHelper.getChatOwlPreferences()
            .set(PreferenceHelper.Key.IS_FIRST_TIME_OPEN, false)
        initAuth(email)
    }

    var currentEmail: String? = null

    fun signUp(nickname: String, email: String) {
        currentEmail = email
        val attributes = CognitoUserAttributes()
        attributes.addAttribute(
            ATTRIBUTE_NICKNAME, nickname
        )
        attributes.addAttribute(
            ATTRIBUTE_EMAIL,
            email
        )

        state.set(ATTRIBUTE_EMAIL, email)
        _isLoading.value = true
        userPool.signUpInBackground(email, DUMMY_PASSWORD, attributes, null, signUpHandler)
    }

    private fun storeEmail(email: String?) {
        if (!email.isNullOrEmpty()) {
            PreferenceHelper.getChatOwlPreferences()
                .set(PreferenceHelper.Key.USER_EMAIL, email)
        }
    }

    private var signUpHandler: SignUpHandler = object : SignUpHandler {

        override fun onSuccess(user: CognitoUser, signUpResult: SignUpResult) {
            PreferenceHelper.getChatOwlPreferences()
                .set(PreferenceHelper.Key.IS_FIRST_TIME_OPEN, true)
            initAuth(user.userId)
        }

        override fun onFailure(exception: java.lang.Exception) {
            _isLoading.value = false
            if (exception is UsernameExistsException) {
                _navigate.value =
                    SignUpFragmentDirections.actionSignupToLogin().setEmail(currentEmail)
            } else {
                _errorMessage.value = R.string.authentication_error
            }
        }
    }



    private var authenticationHandler: AuthenticationHandler = object : AuthenticationHandler {
        override fun onSuccess(cognitoUserSession: CognitoUserSession, device: CognitoDevice?) {
            Log.d(TAG, "onSuccess")
            storeEmail(currentEmail)
            PreferenceHelper.getChatOwlPreferences()
                .set(ACCESS_TOKEN, cognitoUserSession.accessToken.jwtToken)
            _isLoading.value = false
            _loginSuccess.value = true

            val accessToken = PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.ACCESS_TOKEN, "") ?: ""
            if (accessToken.isNotEmpty()) {
                viewModelScope.fetch({
                    ChatOwlUserRepository.sendTracking(
                        PostEventTrackingRequest.SimpleBuilder(
                            PostEventTrackingRequest.Companion.EventDataName.LoggedIn
                        ).build()
                    )
                }, {}, {})
            }

        }

        override fun getAuthenticationDetails(
            authenticationContinuation: AuthenticationContinuation,
            username: String
        ) {
            Log.d(TAG, "getAuthenticationDetails")
            state.set(ATTRIBUTE_EMAIL, username)
            val authenticationParameters = hashMapOf(AUTHENTICATION_PARAMETER_USERNAME to username)
            val authenticationDetails =
                AuthenticationDetails(username, authenticationParameters, null)
            authenticationContinuation.setAuthenticationDetails(authenticationDetails)
            authenticationContinuation.continueTask()
        }

        override fun getMFACode(multiFactorAuthenticationContinuation: MultiFactorAuthenticationContinuation) {
            _isLoading.value = false
            Log.d(TAG, "getMFACode")
        }

        override fun onFailure(e: Exception) {
            Log.d(TAG, "onFailure")
            _isLoading.value = false
            _loginSuccess.value = false
        }

        override fun authenticationChallenge(continuation: ChallengeContinuation) {
            Log.d(TAG, "authenticationChallenge")
            _isLoading.value = false
            if (continuation.challengeName == CUSTOM_CHALLENGE) {
                _challengeContinuation.value = continuation
            }
        }
    }

    fun onLogout() {
        val accessToken = PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.ACCESS_TOKEN, "") ?: ""
        if (accessToken.isNotEmpty()) {
            viewModelScope.fetch({
                ChatOwlUserRepository.sendTracking(
                    PostEventTrackingRequest.SimpleBuilder(
                        PostEventTrackingRequest.Companion.EventDataName.LoggedOut
                    ).build()
                )
            }, {}, {})
        }


        //erase FirebaseId
        viewModelScope.fetch({
            ChatOwlUserRepository.clearFirebaseToken()
        }, {
            continueOnLogout()
        }, {
            continueOnLogout()
        })
    }

    private fun continueOnLogout() {
        //clean Secure mode
        SecureAppUtils.cleanSecureMode()
        // Cognito sign out
        userPool.currentUser.signOut()
        // Clear preferences
        PreferenceHelper.clearSharedPreferences()
        state.set(ATTRIBUTE_EMAIL, null)
        // Clear chat persistence and nullify socket
        SocketManager.resetInstance()
        // Clear all database tables
        viewModelScope.launch(Dispatchers.IO) {
            ChatOwlDatabase.getInstance(getApplication()).clearAllTables()
        }

        // Navigate to onboarding
        _navigate.value = LogoutDialogFragmentDirections.actionGlobalIndexFragments()
    }

    fun resendCode() {
        if (email.value.isNullOrBlank()) {
            _loginSuccess.value = false
        } else {
            initAuth(email.value)
            val acc = _accSentCodes.value ?: 0
            _accSentCodes.value = acc + 1
            _errorMessage.value = R.string.code_sent
        }
    }

    fun sendCode(code: String) {
        if (email.value.isNullOrBlank()) {
            _loginSuccess.value = false
        } else {
            challengeContinuation.value?.let { challenge ->
                _isLoading.value = true
                challenge.setChallengeResponse(CHALLENGE_ANSWER, code)
                challenge.continueTask()
            }
        }
    }
}
