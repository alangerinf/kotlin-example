package com.chatowl.presentation.toolbox.mediaexercise

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.databinding.DialogRateBinding
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_rate_send.view.*
import kotlinx.android.synthetic.main.layout_rate_submit.view.*


class RateDialogFragment : BottomSheetDialogFragment() {

    private val TAG = RateDialogFragment::class.java.simpleName

    private lateinit var binding: DialogRateBinding

    private lateinit var viewModel: RateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set the window no floating style
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        binding = DataBindingUtil.inflate(
            inflater.cloneInContext(contextThemeWrapper),
            R.layout.dialog_rate,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    private val DELAY_TO_CLOSE = 1000L

    private fun ImageView.enableStart() {
        this.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_star_on
            )
        )
    }

    private fun ImageView.disableStart() {
        this.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_star_off
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val application = ChatOwlApplication.get()
        val args: RateDialogFragmentArgs by navArgs()
        viewModel = ViewModelProvider(this, RateViewModel.Factory(application, args)).get(
            RateViewModel::class.java
        )
        lifecycle.addObserver(viewModel)

        viewModel.currentState.observe(viewLifecycleOwner, {
            binding.layoutSend.visibility = View.GONE
            binding.layoutSubmit.visibility = View.GONE
            binding.layoutThanks.visibility = View.GONE
            binding.layoutNoProblem.visibility = View.GONE
            when (it) {
                RateViewModel.RateState.STATUS_SUBMIT -> binding.layoutSubmit.visibility =
                    View.VISIBLE
                RateViewModel.RateState.STATUS_SEND_FEEDBACK -> binding.layoutSend.visibility =
                    View.VISIBLE
                RateViewModel.RateState.STATUS_THANKS -> {
                    binding.layoutThanks.visibility = View.VISIBLE
                    Handler(Looper.getMainLooper()).postDelayed({
                        findNavController().navigateUp()
                    }, DELAY_TO_CLOSE)
                }
                RateViewModel.RateState.STATUS_NO_PROBLEM -> {
                    binding.layoutNoProblem.visibility = View.VISIBLE
                    Handler(Looper.getMainLooper()).postDelayed({
                        findNavController().navigateUp()
                    }, DELAY_TO_CLOSE)
                }
            }
        })

        viewModel.stars.observe(viewLifecycleOwner, { rate ->
            binding.layoutSubmit.iView_star1.disableStart()
            binding.layoutSubmit.iView_star2.disableStart()
            binding.layoutSubmit.iView_star3.disableStart()
            binding.layoutSubmit.iView_star4.disableStart()
            binding.layoutSubmit.iView_star5.disableStart()

            if (rate >= 1) {
                binding.layoutSubmit.iView_star1.enableStart()
            }
            if (rate >= 2) {
                binding.layoutSubmit.iView_star2.enableStart()
            }
            if (rate >= 3) {
                binding.layoutSubmit.iView_star3.enableStart()
            }
            if (rate >= 4) {
                binding.layoutSubmit.iView_star4.enableStart()
            }
            if (rate == 5) {
                binding.layoutSubmit.iView_star5.enableStart()
            }
        })

        /**
         *  Send state
         */
        binding.layoutSend.tView_skip.setOnClickListener {
            viewModel.pressSendRate()
        }

        binding.layoutSend.btn_send.setOnClickListener {
            viewModel.pressSendRate(binding.layoutSend.eText.text.toString().trim())
        }

        /**
         *  Submit state
         */
        binding.layoutSubmit.tView_dontAskAgain.setOnClickListener {
            viewModel.pressDontAskMeAgain()
        }
        binding.layoutSubmit.btn_submit.setOnClickListener {
            viewModel.pressSubmit()
        }
        binding.layoutSubmit.iView_star1.setOnClickListener {
            viewModel.selectStars(1)
        }
        binding.layoutSubmit.iView_star2.setOnClickListener {
            viewModel.selectStars(2)
        }
        binding.layoutSubmit.iView_star3.setOnClickListener {
            viewModel.selectStars(3)
        }
        binding.layoutSubmit.iView_star4.setOnClickListener {
            viewModel.selectStars(4)
        }
        binding.layoutSubmit.iView_star5.setOnClickListener {
            viewModel.selectStars(5)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener {
            val bottomSheetDialog: BottomSheetDialog? = it as? BottomSheetDialog
            val bottomSheetBehavior = bottomSheetDialog?.behavior
            bottomSheetBehavior?.state = STATE_EXPANDED
            bottomSheetBehavior?.setDraggable(false)
        }
        return dialog
    }

}
