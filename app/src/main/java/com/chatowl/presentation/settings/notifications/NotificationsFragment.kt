package com.chatowl.presentation.settings.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chatowl.R
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_notifications.*

class NotificationsFragment : ViewModelFragment<NotificationsViewModel>(NotificationsViewModel::class.java) {

    override fun getScreenName() = "Notification preferences"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_notifications_item_session_due.setOnCheckedChangeListener {
            viewModel.updateSessionDueNotifications(it)
        }
        fragment_notifications_item_exercise_due.setOnCheckedChangeListener {
            viewModel.updateExerciseDueNotifications(it)
        }
        fragment_notifications_item_new_exercise.setOnCheckedChangeListener {
            viewModel.updateNewExerciseNotifications(it)
        }
        fragment_notifications_item_image_of_the_day.setOnCheckedChangeListener {
            viewModel.updateImageOfTheDayNotifications(it)
        }
        fragment_notifications_item_quote_of_the_day.setOnCheckedChangeListener {
            viewModel.updateQuoteOfTheDayNotifications(it)
        }
/*        fragment_notifications_item_message_from_therapist.setOnCheckedChangeListener {
            viewModel.updateMessageFromTherapistNotifications(it)
        }*/
    }

    override fun addObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            fragment_notifications_linear_progress_indicator.visibility = if(isLoading) View.VISIBLE else View.GONE
        })
        viewModel.notificationPreferences.observe(viewLifecycleOwner, { preferences ->
            fragment_notifications_item_session_due.isChecked = preferences.sessionDue
            fragment_notifications_item_exercise_due.isChecked = preferences.exerciseDue
            fragment_notifications_item_new_exercise.isChecked = preferences.newExerciseInToolbox
            fragment_notifications_item_image_of_the_day.isChecked = preferences.imageOfTheDay
            fragment_notifications_item_quote_of_the_day.isChecked = preferences.quoteOfTheDay
            //fragment_notifications_item_message_from_therapist.isChecked = preferences.messageFromTherapist
        })
    }

    override fun removeObservers() {
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
    }

}