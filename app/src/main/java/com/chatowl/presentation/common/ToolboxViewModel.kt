package com.chatowl.presentation.common

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chatowl.data.entities.toolbox.ToolSubtype
import com.chatowl.data.entities.toolbox.ToolboxItem
import com.chatowl.presentation.common.extensions.lowerCase

abstract class ToolboxViewModel(application: Application) : BaseViewModel(application) {

    open val showSearchField: LiveData<Boolean> get() = _showSearchField
    private val _showSearchField = MutableLiveData(false)

    open val searchFilter: LiveData<String> get() = _searchFilter
    private val _searchFilter = MutableLiveData("")

    val audioFilter: LiveData<Boolean> get() = _audioFilter
    private val _audioFilter = MutableLiveData(false)

    val imageFilter: LiveData<Boolean> get() = _imageFilter
    private val _imageFilter = MutableLiveData(false)

    val quoteFilter: LiveData<Boolean> get() = _quoteFilter
    private val _quoteFilter = MutableLiveData(false)

    val videoFilter: LiveData<Boolean> get() = _videoFilter
    private val _videoFilter = MutableLiveData(false)

    val lengthShortFilter: LiveData<Boolean> get() = _lengthShortFilter
    private val _lengthShortFilter = MutableLiveData(false)

    val lengthMediumFilter: LiveData<Boolean> get() = _lengthMediumFilter
    private val _lengthMediumFilter = MutableLiveData(false)

    val lengthLongFilter: LiveData<Boolean> get() = _lengthLongFilter
    private val _lengthLongFilter = MutableLiveData(false)

    open fun areAnyMediaFiltersSelected(): Boolean {
        return audioFilter.value ?: false ||
                imageFilter.value ?: false ||
                quoteFilter.value ?: false ||
                videoFilter.value ?: false
    }

    open fun areAnyLengthFiltersSelected(): Boolean {
        return lengthShortFilter.value ?: false ||
                lengthMediumFilter.value ?: false ||
                lengthLongFilter.value ?: false
    }

    fun onFilterSearchChanged(searchString: String) {
        _searchFilter.value = searchString
    }

    fun onFilterAudioClicked() {
        _audioFilter.value = audioFilter.value?.not() ?: false
    }

    fun onFilterImageClicked() {
        _imageFilter.value = imageFilter.value?.not() ?: false
    }

    fun onFilterQuoteClicked() {
        _quoteFilter.value = quoteFilter.value?.not() ?: false
    }

    fun onFilterVideoClicked() {
        _videoFilter.value = videoFilter.value?.not() ?: false
    }

    fun onFilterShortClicked() {
        _lengthShortFilter.value = lengthShortFilter.value?.not() ?: false
    }

    fun onFilterMediumClicked() {
        _lengthMediumFilter.value = lengthMediumFilter.value?.not() ?: false
    }

    fun onFilterLongClicked() {
        _lengthLongFilter.value = lengthLongFilter.value?.not() ?: false
    }

    val isAllFiltersEnabled: LiveData<Boolean> get() = _isAllFiltersEnabled
    private val _isAllFiltersEnabled = MutableLiveData(false)

    val isMediaFilterEnabled: LiveData<Boolean> get() = _isMediaFilterEnabled
    private val _isMediaFilterEnabled = MutableLiveData(false)

    val isAudioFilterEnabled: LiveData<Boolean> get() = _isAudioFilterEnabled
    private val _isAudioFilterEnabled = MutableLiveData(false)

    val isImageFilterEnabled: LiveData<Boolean> get() = _isImageFilterEnabled
    private val _isImageFilterEnabled = MutableLiveData(false)

    val isQuoteFilterEnabled: LiveData<Boolean> get() = _isQuoteFilterEnabled
    private val _isQuoteFilterEnabled = MutableLiveData(false)

    val isVideoFilterEnabled: LiveData<Boolean> get() = _isVideoFilterEnabled
    private val _isVideoFilterEnabled = MutableLiveData(false)

    val isDurationFilterEnabled: LiveData<Boolean> get() = _isDurationFilterEnabled
    private val _isDurationFilterEnabled = MutableLiveData(false)

    val isShortFilterEnabled: LiveData<Boolean> get() = _isShortFilterEnabled
    private val _isShortFilterEnabled = MutableLiveData(false)

    val isMediumFilterEnabled: LiveData<Boolean> get() = _isMediumFilterEnabled
    private val _isMediumFilterEnabled = MutableLiveData(false)

    val isLongFilterEnabled: LiveData<Boolean> get() = _isLongFilterEnabled
    private val _isLongFilterEnabled = MutableLiveData(false)


    init {
        _isAllFiltersEnabled.value = false

        _isMediaFilterEnabled.value = false
        _isAudioFilterEnabled.value = false
        _isImageFilterEnabled.value = false
        _isQuoteFilterEnabled.value = false
        _isVideoFilterEnabled.value = false


        _isDurationFilterEnabled.value = false
        _isShortFilterEnabled.value = false
        _isMediumFilterEnabled.value = false
        _isLongFilterEnabled.value = false
    }

    fun filterToolboxItemList(toolboxToolList: List<ToolboxItem>): List<ToolboxItem> {
        var filteredToolboxList = toolboxToolList

        filteredToolboxList.forEach {
            toolboxItem ->
            when (toolboxItem.tool.toolSubtype) {
                ToolSubtype.AUDIO.name.lowerCase() -> {
                    _isAllFiltersEnabled.value = true
                    _isMediaFilterEnabled.value = true
                    _isAudioFilterEnabled.value = true
                }
                ToolSubtype.IMAGE.name.lowerCase() -> {
                    _isAllFiltersEnabled.value = true
                    _isMediaFilterEnabled.value = true
                    _isImageFilterEnabled.value = true
                }
                ToolSubtype.QUOTE.name.lowerCase() -> {
                    _isAllFiltersEnabled.value = true
                    _isMediaFilterEnabled.value = true
                    _isQuoteFilterEnabled.value = true
                }
                ToolSubtype.VIDEO.name.lowerCase() -> {
                    _isAllFiltersEnabled.value = true
                    _isMediaFilterEnabled.value = true
                    _isVideoFilterEnabled.value = true
                }
            }

            val duration = toolboxItem.tool.duration
            when {
                duration ?: 301 <= 300 || duration == null -> {
                    _isAllFiltersEnabled.value = true
                    _isDurationFilterEnabled.value = true
                    _isShortFilterEnabled.value = true
                }
                duration in 301..600 -> {
                    _isAllFiltersEnabled.value = true
                    _isDurationFilterEnabled.value = true
                    _isMediumFilterEnabled.value = true
                }
                duration > 600 -> {
                    _isAllFiltersEnabled.value = true
                    _isDurationFilterEnabled.value = true
                    _isLongFilterEnabled.value = true
                }
            }
        }

        if (areAnyMediaFiltersSelected()) {
            filteredToolboxList = filteredToolboxList.filter { toolboxItem ->
                (audioFilter.value == true && toolboxItem.tool.toolSubtype == ToolSubtype.AUDIO.name.lowerCase()) ||
                        (imageFilter.value == true && toolboxItem.tool.toolSubtype == ToolSubtype.IMAGE.name.lowerCase()) ||
                        (quoteFilter.value == true && toolboxItem.tool.toolSubtype == ToolSubtype.QUOTE.name.lowerCase()) ||
                        (videoFilter.value == true && toolboxItem.tool.toolSubtype == ToolSubtype.VIDEO.name.lowerCase())
            }.toMutableList()
        }


        if (areAnyLengthFiltersSelected()) {
            filteredToolboxList = filteredToolboxList.filter {
                ((lengthShortFilter.value == true && (it.tool.duration ?: 301 <= 300 || it.tool.duration == null)) ||
                        (lengthMediumFilter.value == true && (it.tool.duration ?: 0 in 301..600)) ||
                        (lengthLongFilter.value == true && (it.tool.duration ?: 0 > 600)))
            }.toMutableList()
        }

        filteredToolboxList = filteredToolboxList.filter {
            searchFilter.value.isNullOrBlank() ||
                    it.tool.title.contains(searchFilter.value ?: "", true)
        }.toMutableList()

        return filteredToolboxList
    }

    fun showSearchField() {
        _showSearchField.value = true
    }

    fun hideSearchField() {
        _showSearchField.value = false
    }

}