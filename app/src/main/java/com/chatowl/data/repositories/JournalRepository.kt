package com.chatowl.data.repositories

import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import com.chatowl.data.api.chatowl.ChatOwlAPIClient
import com.chatowl.data.database.dao.JournalDao
import com.chatowl.data.entities.journal.*
import com.chatowl.data.entities.journal.gallery.GalleryItem
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.fetch
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.lang.Exception
import java.net.URLConnection
import kotlin.random.Random

class JournalRepository(private val journalDao: JournalDao) {

    private val TAG = JourneyRepository::class.java.simpleName

    private val client = ChatOwlAPIClient

    fun isImageFile(path: String?): Boolean {
        val mimeType: String = URLConnection.guessContentTypeFromName(path)
        return mimeType.startsWith("image")
    }

    fun isAudioFile(path: String?): Boolean {
        val mimeType: String = URLConnection.guessContentTypeFromName(path)
        return mimeType.startsWith("audio")
    }

    fun isVideoFile(path: String?): Boolean {
        val mimeType: String = URLConnection.guessContentTypeFromName(path)
        return mimeType.startsWith("video")
    }

    suspend fun sendItem(galleryItem: GalleryItem): List<String> {
        val inputStream: InputStream? =
            ChatOwlApplication.get().contentResolver.openInputStream(galleryItem.contentUri)
        val content = inputStream?.readBytes()
        val requestFile =
            content!!.toRequestBody(galleryItem.mimeType.toMediaTypeOrNull(), 0, content.size)
        val body =
            MultipartBody.Part.createFormData("mediaFile", galleryItem.displayName, requestFile)
        return ChatOwlAPIClient.uploadFile(body).data
    }

    fun getItem(sourceUri: Uri): GalleryItem {
        val file = sourceUri.toFile()
        return GalleryItem(
            -1,
            sourceUri.lastPathSegment ?: file.name,
            when {
                isVideoFile(file.path) -> MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                isImageFile(file.path) -> MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                isAudioFile(file.path) -> MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
                else -> MediaStore.Files.FileColumns.MEDIA_TYPE_NONE
            },
            URLConnection.guessContentTypeFromName(file.path),
            sourceUri
        )
    }

    var isSyncWithClientUsed = false
    @ExperimentalStdlibApi
    fun syncWithClient() {
        if (!isSyncWithClientUsed) {
            CoroutineScope(Dispatchers.IO).launch {
                isSyncWithClientUsed = true
                try {
                    journalDao.getJournals().forEach { journal ->
                        when {
                            journal.draftNoSaved -> {
                                for (i in journal.components.indices) {
                                    if (journal.components[i].mediaUrl.isNullOrEmpty() && journal.components[i].type.uppercase() != EntryItemType.TEXT.name) {
                                        //sincronizar y guardar
                                        val item = getItem(journal.components[i].uri!!.toUri())
                                        runBlocking {
                                            fetch({ sendItem(item) }, {
                                                journal.components[i].mediaUrl = it.first()
                                                Log.d(
                                                    TAG,
                                                    "upload file done: ${journal.components[i].uri} :" + journal +" "+ it.firstOrNull()
                                                )
                                            }, {
                                                // send exeption to try catch
                                                throw Exception("\"upload file failed: ${journal.components[i].uri} :\" + it.toString()")
                                            })
                                        }
                                    }
                                }
                                Log.d(TAG, "upload finished, continue saving draft "+journal.toString())
                                val savedJournal = client.saveJournal(journal).data
                                for (i in savedJournal.components.indices) {
                                    savedJournal.components[i].id = Random.nextInt(Int.MAX_VALUE)
                                    savedJournal.components[i].uri = journal.components[i].uri
                                }
                                savedJournal.draftNoSaved = false
                                savedJournal.eUpdated = false
                                journalDao.insertJournal(savedJournal)
                                journalDao.deleteJournalById(journal.id)
                                Log.d(TAG, "journal.draftNoSaved ${journal.id} ")
                            }
                            journal.eDeleted -> {
                                client.deleteJournal(journal.id)
                                journal.eDeleted = false
                                journalDao.deleteJournalById(journal.id)
                                Log.d(TAG, "journal.eDeleted ${journal.id} ")
                            }
                            journal.eUpdated -> {
                                for (i in journal.components.indices) {
                                    if (journal.components[i].mediaUrl.isNullOrEmpty() && journal.components[i].type.uppercase() != EntryItemType.TEXT.name) {
                                        //sincronizar y guardar
                                        val item = getItem(journal.components[i].uri!!.toUri())
                                        runBlocking {
                                            fetch({ sendItem(item) }, {
                                                journal.components[i].mediaUrl = it.first()
                                                Log.d(
                                                    TAG,
                                                    "upload file done: ${journal.components[i].uri} :" + journal +" "+ it.firstOrNull()
                                                )
                                            }, {
                                                // send exeption to try catch
                                                throw Exception("\"upload file failed: ${journal.components[i].uri} :\" + it.toString()")
                                            })
                                        }
                                    }
                                }
                                client.updateJournal(journal)
                                journal.eUpdated = false
                                journalDao.updateJournal(journal)
                                Log.d(TAG, "journal.eUpdated ${journal.id} ")
                            }
                        }

                    }
                } catch (e: Exception) {
                    Log.e(TAG, "sync failed $e")
                } finally {
                    isSyncWithClientUsed = true
                }
            }
        }

    }

    @ExperimentalStdlibApi
    fun getJournalsLiveData(): LiveData<List<Journal>> {
        syncWithClient()
        return journalDao.getJournalsLiveData()
    }

    @ExperimentalStdlibApi
    suspend fun getJournals(pageNumber: Int): Int {
        val response = client.getJournals(pageNumber = pageNumber).data
        val journals = response.journals
        journals.forEach { remoteJournal ->
            //si hay entries
            val localJournal = journalDao.getJournalById(remoteJournal.id) //lista de entries

            localJournal?.components?.forEach { localEntry ->
                //buscamos localmente y actualizamos
                val remoteEntryToModify = remoteJournal.components.find { remoteEntry ->
                    (remoteEntry.mediaUrl == localEntry.mediaUrl || remoteEntry.content == localEntry.content)
                            &&
                            remoteEntry.type == localEntry.type
                }
                //uri field is the same than before
                localEntry.id = remoteEntryToModify?.id ?: 0
                localEntry.order = remoteEntryToModify?.order ?: 0
                localEntry.content = remoteEntryToModify?.content
                localEntry.mediaUrl = remoteEntryToModify?.mediaUrl
                localEntry.type = remoteEntryToModify?.type ?: ""
            }
            remoteJournal.components = localJournal?.components ?: emptyList()
        }

        journalDao.insertJournalList(journals)
        return response.total
    }

    @ExperimentalStdlibApi
    suspend fun searchJournals(search: String, pageNumber: Int): GetJournalsResponse {
        return client.getJournals(search = search, pageNumber = pageNumber).data
    }

    suspend fun getJournalById(journalId: String): Journal {
        return journalDao.getJournalById(journalId) ?: Journal()
    }

    suspend fun saveJournal(journal: Journal) {
        Log.d(TAG, "saveJournal($journal)")
        if (journal.id == DRAFT_ID) {
            Log.d(TAG, "journal 1")
            var savedJournal: Journal = journal
            try {
                savedJournal = client.saveJournal(journal).data
                Log.d(TAG, "draft client saved")
            } catch (e: Exception) {
                savedJournal.id = "DraftSaved_" + Random.nextInt(Int.MAX_VALUE)
                savedJournal.draftNoSaved = true
                Log.d(TAG, "mark as updated exception ")
            } finally {
                for (i in savedJournal.components.indices) {
                    savedJournal.components[i].id = Random.nextInt(Int.MAX_VALUE)
                    savedJournal.components[i].uri = journal.components[i].uri
                }
                journalDao.saveJournalAndDeleteDraft(savedJournal)
                Log.d(TAG, "journalDao.saveJournalAndDeleteDraft ")
            }
        } else {
            Log.d(TAG, "journal 2")
            var updatedJournal: Journal = journal.copy()
            try {
                if (journal.draftNoSaved) {
                    Log.d(TAG, "saveJournal journal.draftNoSaved")
                    updatedJournal = client.saveJournal(journal).data
                } else {
                    Log.d(TAG, "saveJournal NO journal.draftNoSaved")
                    updatedJournal = client.updateJournal(journal).data
                }
            } catch (e: Exception) {
                if (!journal.draftNoSaved) {
                    Log.d(TAG, "update eUpdated to true")
                    updatedJournal.eUpdated = true
                }
                Log.d(TAG, "eUpdate = true")
            } finally {
                Log.d(TAG, "journal client updated")
                for (i in updatedJournal.components.indices) {
                    updatedJournal.components[i].id = Random.nextInt(Int.MAX_VALUE)
                }
                Log.d(TAG, "journal updated in the db ")
                if (updatedJournal.id != journal.id) {
                    deleteJournal(journal.id)
                    Log.d(TAG, "delete jorunal ${journal.id}")
                }
                journalDao.insertJournal(updatedJournal)
            }
        }
    }

    suspend fun saveDraft(draft: Journal) {
        journalDao.insertJournal(draft)
    }

    suspend fun deleteDraft() {
        journalDao.deleteJournalById(DRAFT_ID)
    }

    suspend fun deleteJournal(journalId: String) {
        Log.d(TAG, "delete $journalId")
        try {
            client.deleteJournal(journalId)
            Log.d(TAG, "delete $journalId try")
            journalDao.deleteJournalById(journalId)
            Log.d(TAG, "delete $journalId deleting")
        } catch (e: Exception) {
            val journal = getJournalById(journalId)
            if (journal.draftNoSaved) {
                Log.d(TAG, "delete $journalId journal.draftNoSaved")
                journalDao.deleteJournalById(journalId)
            } else {
                Log.d(TAG, "delete $journalId no journal.draftNoSaved")
                journal.eDeleted = true
                journalDao.updateJournal(journal)
            }
            Log.d(TAG, "delete $journalId catch")
        }
    }
}