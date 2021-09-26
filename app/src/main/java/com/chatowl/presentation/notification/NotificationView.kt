package com.chatowl.presentation.notification

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.chatowl.R
import com.chatowl.data.entities.notification.ChatOwlNotification
import com.chatowl.data.entities.notification.NotificationAlertType

class NotificationView() {

    companion object {
        private val NOTIFICATION_VIEW_TIMEOUT = 4000L
        private var fadeAnimationIn: Animation? = null
        private var fadeAnimationOut: Animation? = null

        fun inflate(
            layoutInflater: LayoutInflater,
            notification: ChatOwlNotification,
            rootView: ViewGroup,
            context: Context,
            notificationClickListener: NotificationClickListener
        ): View {
            val notificationLayout = layoutInflater.inflate(R.layout.notification_layout, rootView, false)
            rootView.addView(notificationLayout)

            setData(notificationLayout, notification, context)

            setAnimations(context, rootView, notificationLayout)
            setDismissTimeout(notificationLayout)
            notificationLayout.startAnimation(fadeAnimationIn)
            setListeners(notificationLayout, notification,  notificationClickListener)
            return notificationLayout
        }

        private fun setData(notificationLayout: View, notification: ChatOwlNotification, context: Context) {
            val tvTitle = notificationLayout.findViewById<TextView>(R.id.tvNotificationTitle)
            val tvBody = notificationLayout.findViewById<TextView>(R.id.tvNotificationBody)
            val ivIcon = notificationLayout.findViewById<ImageView>(R.id.ivNotificationIcon)

            tvTitle.setText(notification.alert.title)
            tvBody.setText(notification.alert.body)
            setNotificationIcon(ivIcon, notification, context)
        }

        private fun setNotificationIcon(ivIcon:ImageView, notification: ChatOwlNotification, context: Context) {

            when (notification.alert.type.toLowerCase()) {
                NotificationAlertType.ERROR.name.toLowerCase() -> {
                    ivIcon.setImageResource(R.drawable.ic_warning_circle)
                    ivIcon.setBackgroundColor(context.resources.getColor(R.color.red))
                }
                NotificationAlertType.MESSAGE.name.toLowerCase() -> {
                    ivIcon.setImageResource(R.drawable.ic_message)
                    ivIcon.setBackgroundColor(context.resources.getColor(R.color.lightPink))
                }
                NotificationAlertType.NEW_ITEM.name.toLowerCase() -> {
                    ivIcon.setImageResource(R.drawable.ic_new_item)
                    ivIcon.setBackgroundColor(context.resources.getColor(R.color.violet))
                }
                NotificationAlertType.DUE_ITEM.name.toLowerCase() -> {
                    ivIcon.setImageResource(R.drawable.ic_due_item)
                    ivIcon.setBackgroundColor(context.resources.getColor(R.color.yellow))
                }
                else -> {
                    ivIcon.setImageResource(R.drawable.ic_message)
                    ivIcon.setBackgroundColor(context.resources.getColor(R.color.green))
                }
            }
        }

        private fun setDismissTimeout(notificationView:View){
            Handler(Looper.getMainLooper()).postDelayed({
                //notificationView.visibility = View.INVISIBLE
                notificationView.startAnimation(fadeAnimationOut)
            }, NOTIFICATION_VIEW_TIMEOUT)
        }

        private fun setAnimations(context: Context, parent:ViewGroup, notificationLayout: View){
            fadeAnimationIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            fadeAnimationOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            fadeAnimationOut?.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    p0?.cancel()
                    parent.removeView(notificationLayout)
                }

                override fun onAnimationRepeat(p0: Animation?) {}
            })
        }

        private fun setListeners(notificationLayout: View, notification: ChatOwlNotification, notificationClickListener: NotificationClickListener){
            notificationLayout.setOnClickListener{
                notificationClickListener.onClick(notification)
            }
        }

    }

    public interface NotificationClickListener{
        fun onClick(notification: ChatOwlNotification)
    }
}