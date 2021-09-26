package com.chatowl.presentation.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.data.api.chat.TOUR_CHAT_WELCOME_FLOW_ID
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.databinding.FragmentWelcomeBinding
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.main.MainViewModel

class WelcomeFragment : ViewModelFragment<WelcomeViewModel>(WelcomeViewModel::class.java) {

    override fun getScreenName() = "WelcomeFragment"

    private val mainActivityViewModel: MainViewModel by activityViewModels()

    private lateinit var binding: FragmentWelcomeBinding

    val args: WelcomeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun setViewModel(): WelcomeViewModel {
        val application = ChatOwlApplication.get()
        return ViewModelProvider(
            this,
            WelcomeViewModel.Factory(application, ChatOwlUserRepository)
        ).get(
            WelcomeViewModel::class.java
        )
    }

    override fun addObservers() {

        mainActivityViewModel.isChatMuted.observe(viewLifecycleOwner, { isMuted ->

            binding.iViewVolume.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (isMuted) R.drawable.ic_volume_welcome_off
                    else R.drawable.ic_volume_welcome_on
                )
            )
        })

        binding.iViewVolume.setOnClickListener {
            mainActivityViewModel.setChatMuted(!mainActivityViewModel.isChatMuted.value!!)
        }

        binding.tViewTitle.text = resources.getString(
            R.string.welcome_title,
            args.name
        )
        binding.btnOk.setOnClickListener {
            openTourChat(TOUR_CHAT_WELCOME_FLOW_ID)
        }
        binding.btnNoThanks.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun removeObservers() {

    }

    private fun openTourChat(flowId: Int) {
        mainActivityViewModel._openChat_flowId.value = flowId
        findNavController().navigateUp()
    }

}