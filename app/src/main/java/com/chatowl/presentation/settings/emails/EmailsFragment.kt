package com.chatowl.presentation.settings.emails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_emails.*

class EmailsFragment : ViewModelFragment<EmailsViewModel>(EmailsViewModel::class.java) {

    override fun getScreenName() = "Email preferences"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_emails, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_emails_item_session_due.setOnCheckedChangeListener {
            viewModel.updateSessionDueEmails(it)
        }
        fragment_emails_item_exercise_due.setOnCheckedChangeListener {
            viewModel.updateExerciseDueEmails(it)
        }
        fragment_emails_item_new_exercise.setOnCheckedChangeListener {
            viewModel.updateNewExerciseEmails(it)
        }
        fragment_emails_item_image_of_the_day.setOnCheckedChangeListener {
            viewModel.updateImageOfTheDayEmails(it)
        }
        fragment_emails_item_quote_of_the_day.setOnCheckedChangeListener {
            viewModel.updateQuoteOfTheDayEmails(it)
        }
/*        fragment_emails_item_message_from_therapist.setOnCheckedChangeListener {
            viewModel.updateMessageFromTherapistEmails(it)
        }*/
    }

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            it?.let { navDirections ->
                    findNavController().navigate(navDirections)
            }
        })
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            showSnackBarMessage(it)
        })
        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            fragment_emails_linear_progress_indicator.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        })
        viewModel.emailPreferences.observe(viewLifecycleOwner, { preferences ->
            fragment_emails_item_session_due.isChecked = preferences.sessionDue
            fragment_emails_item_exercise_due.isChecked = preferences.exerciseDue
            fragment_emails_item_new_exercise.isChecked = preferences.newExerciseInToolbox
            fragment_emails_item_image_of_the_day.isChecked = preferences.imageOfTheDay
            fragment_emails_item_quote_of_the_day.isChecked = preferences.quoteOfTheDay
            //fragment_emails_item_message_from_therapist.isChecked = preferences.messageFromTherapist
        })
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.errorMessage.removeObservers(viewLifecycleOwner)
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
        viewModel.emailPreferences.removeObservers(viewLifecycleOwner)
    }
}

