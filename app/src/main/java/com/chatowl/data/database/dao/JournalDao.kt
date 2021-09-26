package com.chatowl.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.chatowl.data.entities.journal.DRAFT_ID
import com.chatowl.data.entities.journal.Journal

@Dao
interface JournalDao {

    @Update
    suspend fun updateJournal(journal: Journal)

    @Transaction
    @Query("SELECT * FROM journal WHERE eDeleted = 0 ORDER BY date")
    fun getJournalsLiveData(): LiveData<List<Journal>>

    @Transaction
    @Query("SELECT * FROM journal ORDER BY date")
    fun getJournals(): List<Journal>

    @Query("SELECT * FROM journal WHERE id =:journalId")
    suspend fun getJournalById(journalId: String): Journal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertJournalList(journals: List<Journal>)

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: Journal)

    @Query("DELETE FROM journal WHERE id =:journalId")
    suspend fun deleteJournalById(journalId: String)

    @Transaction
    suspend fun saveJournalAndDeleteDraft(journal: Journal) {
        insertJournal(journal)
        deleteJournalById(DRAFT_ID)
    }
}