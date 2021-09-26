package com.chatowl.presentation.common.widgets.toggle

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.chatowl.R
import kotlin.math.max

open class ToggleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), View.OnClickListener {

    companion object {

        private const val SELECTOR_ANIMATION_DURATION = 200

    }

    interface OnTabSelectedListener {

        fun onTabSelected(@IdRes id: Int)

    }

    private val items = arrayListOf<ToggleItemView>()
    private val visibleItems get() = items.filter { it.visibility != View.GONE }
    private var currentSelected: View? = null
    private var selectorView: View

    private val onTabSelectedListeners = arrayListOf<OnTabSelectedListener>()
    private val constraintSet: ConstraintSet = ConstraintSet()

    init {
        setBackgroundResource(R.drawable.background_dark_rounded_smallish)
        constraintSet.clone(this)
        selectorView = buildSelectorView()
        addView(selectorView, 0, LayoutParams(0, 0))
        constraintSet.applyTo(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = resources.getDimensionPixelSize(R.dimen.tab_view_height)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        update()
    }

    fun setChildVisibility(id: Int, visibility: Int) {
        items.find { it.id == id }?.let {
            it.visibility = visibility
            update()
        }
    }

    private fun update() {
        val count = visibleItems.size
        if (count == 0) {
            return
        }

        constraintSet.clone(this)
        constraintSet.constrainPercentWidth(selectorView.id, 1f / max(count, 1))

        if (count == 1) {
            val item = visibleItems.first()
            constraintSet.connect(item.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(item.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        } else {
            val chainIds = visibleItems.map { it.id }.toIntArray()
            val weights = List(count) { 1f }.toFloatArray()
            constraintSet.createHorizontalChainRtl(ConstraintSet.PARENT_ID, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END, chainIds, weights, ConstraintSet.CHAIN_SPREAD)

        }

        constraintSet.applyTo(this)
    }

    override fun onViewAdded(view: View?) {
        super.onViewAdded(view)
        if (view == null || view == selectorView) {
            return
        }

        if (view.id == View.NO_ID) {
            throw IllegalArgumentException("$view must have an identifier.")
        }

        if (view !is ToggleItemView) {
            throw IllegalArgumentException("TabView child must be of type ${ToggleItemView::class.java.simpleName}.")
        }

        configChild(view)
    }

    private fun configChild(child: ToggleItemView) {
        val params = child.layoutParams as LayoutParams
        params.width = 0
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        child.layoutParams = params
        child.setOnClickListener(this)
        items.add(child)
    }

    private fun buildSelectorView(): View {
        val view = View(context)
        view.id = View.generateViewId()
        view.background = ContextCompat.getDrawable(context, R.drawable.background_primary_dark_outline_rounded_smallish)
        constraintSet.setHorizontalBias(view.id, 0f)
        constraintSet.constrainPercentWidth(view.id, 1f / max(visibleItems.size, 1))
        constraintSet.connect(view.id, ConstraintSet.END, id, ConstraintSet.END)
        constraintSet.connect(view.id, ConstraintSet.START, id, ConstraintSet.START)
        constraintSet.connect(view.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
        constraintSet.connect(view.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
        return view
    }

    fun addOnTabSelectedListener(onTabSelectedListener: OnTabSelectedListener?) {
        if (onTabSelectedListener == null || onTabSelectedListeners.contains(onTabSelectedListener)) {
            return
        }

        onTabSelectedListeners.add(onTabSelectedListener)
    }

    fun select(@IdRes id: Int) {
        find(id)?.let {
            if (!it.isSelected) {
                it.isSelected = false
                it.callOnClick()
            }
        }
    }

    protected fun find(@IdRes id: Int): ToggleItemView? {
        for (view in items) {
            if (view.id == id) {
                return view
            }
        }
        return null
    }

    override fun onClick(view: View) {
        if (view.isSelected) {
            return
        }

        val position = getRealPosition(view)

        if (position == -1) {
            return
        }

        if (currentSelected != null) {
            currentSelected!!.isSelected = false
        }

        currentSelected = view
        currentSelected!!.isSelected = true

        val lastPosition = visibleItems.size - 1
        val selectorHorizontalBias = position / lastPosition.toFloat()
        val transition = AutoTransition()
        transition.duration = SELECTOR_ANIMATION_DURATION.toLong()
        TransitionManager.beginDelayedTransition(this, transition)
        constraintSet.setHorizontalBias(selectorView.id, selectorHorizontalBias)
        constraintSet.applyTo(this)
        if (onTabSelectedListeners.isNotEmpty()) {
            triggerTabListeners(view.id)
        }
    }

    private fun getRealPosition(view: View): Int = visibleItems.indexOfFirst { it == view }

    private fun triggerTabListeners(@IdRes id: Int) {
        for (listener in onTabSelectedListeners) {
            listener.onTabSelected(id)
        }
    }

}