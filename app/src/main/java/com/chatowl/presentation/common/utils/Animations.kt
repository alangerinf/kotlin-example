package com.chatowl.presentation.common.utils

import android.animation.*
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlinx.android.synthetic.main.dialog_feedback.*


object Animations {

    fun getPulseAnimation(innerView: View, outerView: View): AnimatorSet {

        val innerScaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 2f)
        val innerScaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 2f)
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)
        val innerAnimator = ObjectAnimator.ofPropertyValuesHolder(innerView, innerScaleX, innerScaleY, alpha)
        innerAnimator.duration = 3000
        innerAnimator.interpolator = DecelerateInterpolator()
        innerAnimator.repeatCount = ObjectAnimator.INFINITE
        innerAnimator.repeatMode = ObjectAnimator.RESTART

        val outerScaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 4f)
        val outerScaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 4f)
        val outerAnimator = ObjectAnimator.ofPropertyValuesHolder(outerView, outerScaleX, outerScaleY, alpha)
        outerAnimator.duration = 3000
        outerAnimator.interpolator = DecelerateInterpolator()
        outerAnimator.repeatCount = ObjectAnimator.INFINITE
        outerAnimator.repeatMode = ObjectAnimator.RESTART

        val animatorSet = AnimatorSet()
        animatorSet.play(innerAnimator).with(outerAnimator)

        return animatorSet
    }
}
