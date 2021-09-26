package com.chatowl.presentation.common.widgets.toggle

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import com.chatowl.R
import com.chatowl.data.entities.plan.ProgramVersion

class PlanVersionsToggleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ToggleView(context, attrs, defStyleAttr) {

    interface OnSubscriptionTabSelectedListener {
        fun onSubscriptionTabSelected(tab: ProgramVersion)
    }

    private var currentTab: ProgramVersion? = null

    @IdRes
    private val slimId: Int = View.generateViewId()
    @IdRes
    private val fullId: Int = View.generateViewId()

    init {
        // FULL
        val fullTextView = ToggleItemView(context)
        fullTextView.id = fullId
        fullTextView.text = context.getString(R.string.full)
        fullTextView.setTextColor(ContextCompat.getColor(context, R.color.primaryTextColor))
        addView(fullTextView)
        // SLIM
        val slimTextView = ToggleItemView(context)
        slimTextView.id = slimId
        slimTextView.text = context.getString(R.string.slim)
        slimTextView.setTextColor(ContextCompat.getColor(context, R.color.primaryTextColor))
        addView(slimTextView)
    }
    fun setText(
        tab: ProgramVersion?,
        text: CharSequence?
    ) {
        val itemView = find(tab)
        if (itemView != null) {
            itemView.text = text
        }
    }

    fun addOnTabSelectedListener(onTabSelectedListener: OnSubscriptionTabSelectedListener?) {
        if (onTabSelectedListener == null) {
            return
        }

        addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(id: Int) {
                when(id) {
                    slimId -> {
                        currentTab = ProgramVersion.SLIM
                        findViewById<ToggleItemView>(slimId).typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                        findViewById<ToggleItemView>(fullId).typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                        onTabSelectedListener.onSubscriptionTabSelected(ProgramVersion.SLIM)
                    }
                    fullId -> {
                        currentTab = ProgramVersion.FULL
                        findViewById<ToggleItemView>(slimId).typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                        findViewById<ToggleItemView>(fullId).typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                        onTabSelectedListener.onSubscriptionTabSelected(ProgramVersion.FULL)
                    }
                }
            }
        })
    }

    fun getCurrentTab(): ProgramVersion? {
        return currentTab
    }

    fun callClick(tab: ProgramVersion?) {
        val itemView = find(tab)
        if (itemView != null) {
            itemView.isSelected = false
            itemView.callOnClick()
        }
    }

    private fun find(tab: ProgramVersion?): ToggleItemView? {
        if (tab == null) return null
        @IdRes var id = -1
        id = when (tab) {
            ProgramVersion.SLIM -> slimId
            ProgramVersion.FULL -> fullId
            else -> fullId
        }
        return if (id == -1) {
            null
        } else {
            find(id)
        }
    }

}