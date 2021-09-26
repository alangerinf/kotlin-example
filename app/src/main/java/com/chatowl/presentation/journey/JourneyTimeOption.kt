package com.chatowl.presentation.journey

enum class JourneyTimeOption {

    days,
    weeks,
    months;

    fun numberOfDaysPerPage (): Int {
        return when (this) {
            days -> 30
            weeks -> 100
            months -> 300
        }
    }


    fun daysPerFrame(): Float {
        return when (this) {
            days -> 1f
            weeks -> 3f
            months -> 15f
        }
    }

    fun visibleXRange (): Float {
        return when (this) {
            days -> 2.0f
            weeks -> 7.0f
            months -> 50.0f
        }
    }

}