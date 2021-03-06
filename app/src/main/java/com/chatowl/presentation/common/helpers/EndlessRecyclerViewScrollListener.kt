package com.chatowl.presentation.common.helpers

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

abstract class EndlessRecyclerViewScrollListener protected constructor(layoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {
    // The current offset index of data you have loaded
    private var currentPage = 0
    // The total number of items in the dataset after the last load
    private var previousTotalItemCount = 0
    // True if we are still waiting for the last set of data to load.
    private var loading = true

    var isScrollEnabled = true


    private val mLayoutManager: RecyclerView.LayoutManager

    init {
        this.mLayoutManager = layoutManager
    }

    private fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        if (isScrollEnabled) {

            var lastVisibleItemPosition = 0
            val totalItemCount = mLayoutManager.itemCount

            when (mLayoutManager) {
                is StaggeredGridLayoutManager -> {
                    val lastVisibleItemPositions = mLayoutManager.findLastVisibleItemPositions(null)
                    // get maximum element within the list
                    lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions)
                }
                is GridLayoutManager -> lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                is LinearLayoutManager -> lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
            }

            // If it???s still loading, we check to see if the dataset count has
            // changed, if so we conclude it has finished loading and update the current page
            // number and total item count.

            // If it isn???t currently loading, we check to see if we have breached
            // the visibleThreshold and need to reload more data.
            // If we do need to reload some more data, we execute onLoadMore to fetch the data.
            // threshold should reflect how many total columns there are too

            // The minimum amount of items to have below your current scroll position
            // before loading more.

            // If it???s still loading, we check to see if the dataset count has
            // changed, if so we conclude it has finished loading and update the current page
            // number and total item count.
            if (loading && totalItemCount > previousTotalItemCount) {
                loading = false
                previousTotalItemCount = totalItemCount
            }

            // If it isn???t currently loading, we check to see if we have breached
            // the visibleThreshold and need to reload more data.
            // If we do need to reload some more data, we execute onLoadMore to fetch the data.
            // threshold should reflect how many total columns there are too

            // The minimum amount of items to have below your current scroll position
            // before loading more.
            val visibleThreshold = 5

            if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
                currentPage++
                onLoadMore(currentPage, totalItemCount, view)
                loading = true
            }
        }

    }

    // CustomCall whenever performing new searches
    fun resetState() {
        this.currentPage = 0
        this.previousTotalItemCount = 0
        this.loading = true
    }

    // Defines the process for actually loading more data based on page
    protected abstract fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView)
}
