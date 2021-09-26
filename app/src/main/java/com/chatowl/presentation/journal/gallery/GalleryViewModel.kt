package com.chatowl.presentation.journal.gallery

import android.app.Application
import android.content.ContentUris
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chatowl.data.entities.journal.gallery.GalleryItem
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch

class GalleryViewModel(application: Application) : BaseViewModel(application) {

    private val _content = MutableLiveData<List<GalleryItem>>()
    val content: LiveData<List<GalleryItem>> get() = _content

    init {
        fetchImages()
    }

    private fun fetchImages() {
        _isLoading.value = true
        viewModelScope.fetch({
            queryContent()
        }, {
            _isLoading.value = false
            _content.value = it
        }, {
            _isLoading.value = false
            it.printStackTrace()
        })
    }

    private fun queryContent(): List<GalleryItem> {
        val contentList = mutableListOf<GalleryItem>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR ("
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + " AND "
                + MediaStore.Files.FileColumns.DURATION + "<="
                + "60000)")

        val queryUri = MediaStore.Files.getContentUri("external")

        getApplication<Application>().contentResolver.query(
            queryUri,
            projection,
            selection,
            null,
            MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        )?.use { cursor ->

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val mimeTypeColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val mediaType = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

            while (cursor.moveToNext()) {
                // Here we'll use the column indexs that we found above.
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val contentMediaType = cursor.getInt(mediaType)
                val mimeType = cursor.getString(mimeTypeColumn)

                val contentUri = ContentUris.withAppendedId(
                    queryUri,
                    id
                )

                val content = GalleryItem(
                    id,
                    displayName,
                    contentMediaType,
                    mimeType,
                    contentUri
                )
                contentList += content
            }
        }
        return contentList
    }

}
