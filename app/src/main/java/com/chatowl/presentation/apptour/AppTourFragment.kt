package com.chatowl.presentation.apptour

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.data.api.chat.APP_TOUR_FLOW_ID
import com.chatowl.data.entities.apptour.AppTourData
import com.chatowl.data.entities.chat.ChatAction
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.tourchat.TourChatFragment
import com.chatowl.presentation.tourchat.TourChatViewModel
import kotlinx.android.synthetic.main.fragment_app_tour.*

class AppTourFragment() : ViewModelFragment<AppTourViewModel>(AppTourViewModel::class.java) {

    val args: AppTourFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_app_tour, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(viewModel)

    }

    override fun setViewModel(): AppTourViewModel {
        val application = ChatOwlApplication.get()
        val userRepository = ChatOwlUserRepository
        return ViewModelProvider(
            requireActivity(),
            AppTourViewModel.Factory(application, userRepository, args)
        ).get(AppTourViewModel::class.java)
    }

    fun bindData(appTourData: AppTourData) {
        app_tour_first_button.text = appTourData.welcome.firstButtonText
        app_tour_second_button.text = appTourData.welcome.secondButtonText

        app_tour_first_title.text = appTourData.welcome.firstLineTitle
        app_tour_second_title.text = appTourData.welcome.secondLineTitle
        app_tour_subtitle.text = appTourData.welcome.subTitle

        app_tour_first_button.setOnClickListener(View.OnClickListener {
            launchAppTourChat(appTourData)
        })

        app_tour_second_button.setOnClickListener(View.OnClickListener {
            requireActivity().onBackPressed();
        })

    }

    fun launchAppTourChat(appTourData: AppTourData) {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            TourChatFragment.TOUR_CHAT_REQUEST_KEY,
            this
        ) { requestKey, bundle ->
            if (requestKey == TourChatFragment.TOUR_CHAT_REQUEST_KEY) {
                val result = bundle.getParcelable<ChatAction>(TourChatFragment.TOUR_CHAT_RESULT_KEY)
                when {
                    result != null -> {
                        handleChatAction(result)
                    }
                }
            }
        }

        val tourChatFragment = TourChatFragment.newInstance(APP_TOUR_FLOW_ID, appTourData)
        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(
            R.id.app_tour_chat_container,
            tourChatFragment
        )
        transaction.addToBackStack(TourChatFragment.TOUR_CHAT_FRAGMENT_KEY)
        transaction.commit()


        val tourChatBackgroudFragment = AppTourBackgroundFragment.newInstance(appTourData)
        val backgroundChatTransaction = fragmentManager.beginTransaction()
        backgroundChatTransaction.add(
            R.id.app_tour_chat_background_container,
            tourChatBackgroudFragment
        )
        backgroundChatTransaction.addToBackStack("BackgroundTourChatFragment")
        backgroundChatTransaction.commit()
    }

    private fun handleChatAction(action: ChatAction) {
        //TODO nothing yet, actions are handled on AppTourBackgroundFragment
    }

    override fun addObservers() {
        viewModel.appTourData.observe(viewLifecycleOwner, Observer {
            bindData(it)
        })
        viewModel.navigate.observe(viewLifecycleOwner, { navDirection ->
            findNavController().currentDestination?.getAction(navDirection.actionId)?.let {
                findNavController().navigate(navDirection)
            }

        })
        viewModel.goBack.observe(viewLifecycleOwner, { goBack ->
            when{
                goBack -> {
                    requireActivity().onBackPressed();
                }
            }
        })
    }

    override fun removeObservers() {
        viewModel.appTourData.removeObservers(viewLifecycleOwner)
        viewModel.navigate.removeObservers(viewLifecycleOwner)
    }
}