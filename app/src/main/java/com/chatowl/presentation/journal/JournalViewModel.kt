package com.chatowl.presentation.journal

import android.app.Application
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.chatowl.BuildConfig
import com.chatowl.R
import com.chatowl.data.entities.journal.DRAFT_ID
import com.chatowl.data.entities.journal.Journal
import com.chatowl.data.entities.journal.JournalPreview
import com.chatowl.data.entities.journal.asJournalPreviewList
import com.chatowl.data.entities.toolbox.ExerciseInstruction
import com.chatowl.data.repositories.JournalRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.TwoOptionsDialogFragment
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.extensions.lowerCase
import com.chatowl.presentation.main.MainViewModel
import com.chatowl.presentation.toolbox.host.ToolboxHostFragmentDirections
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import okhttp3.internal.toImmutableList
import java.lang.Runnable
import java.net.InetAddress

@ExperimentalStdlibApi
class JournalViewModel(application: Application, private val journalRepository: JournalRepository) :
    BaseViewModel(application) {

    val TAG = JournalViewModel::class.java.simpleName

    companion object {
        const val JOURNAL_PAGE_SIZE = 10
        const val JOURNAL_SEARCH_DELAY = 1000L
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val journalRepository: JournalRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return JournalViewModel(application, journalRepository) as T
        }
    }

    val isSearching: LiveData<Boolean> get() = _isSearching
    private val _isSearching = MutableLiveData(false)

    val isPageLoading: LiveData<Boolean> get() = _isPageLoading
    private val _isPageLoading = MutableLiveData(false)

    private val searchResults: LiveData<List<Journal>> get() = _searchResults
    private val _searchResults = MutableLiveData<List<Journal>>(emptyList())

    @ExperimentalStdlibApi
    private val entries = journalRepository.getJournalsLiveData()
    private val domainItems = MediatorLiveData<List<Journal>>()

    private var currentTotal = 0
    private var persistedTotal = 0
    private var searchTotal = 0
    private var searchJob: Job? = null
    private var searchTerm: String = ""

//    val adapterItems = Transformations.map(entries) { journalEntries ->
//        journalEntries.asJournalPreviewList().sortedWith(compareBy({ !it.isDraft }, { it.date }))
//    }

    init {

        domainItems.addSource(searchResults) {
            domainItems.value = combineResults(searchTerm, searchResults.value, entries.value)
        }

        domainItems.addSource(entries) {
            domainItems.value = combineResults(searchTerm, searchResults.value, entries.value)
        }
        
    }

    val adapterItems = Transformations.map(domainItems) { domainItems ->
        domainItems.asJournalPreviewList()
            .sortedWith(
                compareBy<JournalPreview> { !it.isDraft }
                    .thenByDescending { it.date }
            )
    }

    private fun combineResults(
        searchTerm: String,
        searchResults: List<Journal>?,
        rawJournalList: List<Journal>?
    ): List<Journal> {
        return if (searchTerm.isNotEmpty()) {
            currentTotal = searchTotal
            searchResults ?: emptyList()
        } else {
            currentTotal = persistedTotal
            rawJournalList ?: emptyList()
        }
    }

    fun fetchJournalEntries(pageNumber: Int = 0) {
        if (pageNumber == 0) {
            _isLoading.value = true
            _isPageLoading.value = false
        } else {
            _isLoading.value = false
            _isPageLoading.value = true
        }
        viewModelScope.fetch({
            journalRepository.getJournals(pageNumber = pageNumber)
        }, { total ->
            persistedTotal = total
            _isLoading.value = false
            _isPageLoading.value = false
        }, {
            _isLoading.value = false
            _isPageLoading.value = false
        })
    }

    private fun searchJournalEntries(pageNumber: Int = 0) {
        if (pageNumber == 0) {
            _isSearching.postValue(true)
            _isPageLoading.postValue(true)
        } else {
            _isSearching.postValue(true)
            _isPageLoading.postValue(true)
        }
        viewModelScope.fetch({
            journalRepository.searchJournals(searchTerm, pageNumber)
        }, { getJournalsResponse ->
            searchTotal = getJournalsResponse.total
            _searchResults.value =
                mutableListOf(*searchResults.value?.toTypedArray() ?: emptyArray())
                    .also {
                        it.addAll(getJournalsResponse.journals)
                        it.toImmutableList()
                    }
            _isSearching.value = false
            _isPageLoading.value = false
        }, {
            _isSearching.value = false
            _isPageLoading.value = false
        })
    }

    fun onCreateNewEntryClicked() {
        adapterItems.value?.find { it -> it.isDraft }?.let {
            onJournalClicked(it)
        }?: run {
            _navigate.value =
                ToolboxHostFragmentDirections.actionToolboxHostToNewJournal()
        }
    }

    fun onJournalClicked(entry: JournalPreview) {
        if (entry.id == DRAFT_ID) {
            _navigate.value =
                ToolboxHostFragmentDirections.actionToolboxHostToNewJournal()
        } else {
            domainItems.value?.find { it.id == entry.id }?.let { journal ->
                _navigate.value = ToolboxHostFragmentDirections.actionToolboxHostToEditJournal(journal)
            }
        }
    }

    private fun getURL(fullURL:String):String{
        return fullURL.substring(fullURL.indexOf("://") + 3, fullURL.indexOf(".com") + 4)
    }

    fun onJournalTryToDelete(journal: JournalPreview, activity: FragmentActivity, refresh: Runnable) {
        val dialog = TwoOptionsDialogFragment(
            {
                if (journal.id == DRAFT_ID) deleteDraft()
                else deleteJournal(journal.id)
            },
            {
                refresh.run() //to reload the recycler view
            },
            activity.getString(R.string.delete_entry_title),
            activity.getString(R.string.delete_entry_explanation),
            activity.getString(R.string.delete),
            activity.getString(R.string.cancel)
        )
        dialog.show(activity.supportFragmentManager, dialog.tag)


    }

    private fun deleteDraft() {
        viewModelScope.fetch({
            journalRepository.deleteDraft()
        }, {}, {})
    }

    private fun deleteJournal(journalId: String) {
        _isLoading.value = true
        viewModelScope.fetch({
            journalRepository.deleteJournal(journalId)
        }, {
            _isLoading.value = false
        }, {
            _isLoading.value = false
        })
    }

    fun onScrollEnded() {
        entries.value?.let { entries ->
            val entriesSize = entries.size
            if (isLoading.value == false && entriesSize < currentTotal) {
                val nextPage = entriesSize.div(JOURNAL_PAGE_SIZE)
                if (searchTerm.isNotEmpty()) {
                    searchJournalEntries(pageNumber = nextPage)
                } else {
                    fetchJournalEntries(pageNumber = nextPage)
                }
            }
        }
    }

    fun onSearchFieldChanged(term: String) {
        searchTerm = term
        searchJob?.cancel()
        if (searchTerm.isNotEmpty()) {
            searchJob = viewModelScope.launch(Dispatchers.Default) {
                delay(JOURNAL_SEARCH_DELAY)
                searchJournalEntries()
            }
        } else {
            _searchResults.value = emptyList()
        }
    }

    @ExperimentalStdlibApi
    fun syncWithClient() {
        journalRepository.syncWithClient()
    }
}