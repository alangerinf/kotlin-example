package com.chatowl.presentation.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract

class CustomUseCameraContract(val video:Boolean = false) : ActivityResultContract<Uri, Uri?>() {

    private val MAX_TIME: Int = 60
    private var fileUri: Uri? = null

    override fun createIntent(context: Context, uri: Uri?): Intent {
        fileUri = uri

        val intent =  Intent(getAction())
            .putExtra(MediaStore.EXTRA_OUTPUT, uri)

        when {
             video ->   {
                 intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, MAX_TIME)
                 //intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 5491520L*5)// 5MB * n
                 intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                 intent.putExtra(MediaStore.Video.Thumbnails.HEIGHT, 320);
                 intent.putExtra(MediaStore.Video.Thumbnails.WIDTH, 240);
             }
        }

        return intent
    }

    private fun getAction():String{
        when{
            video -> return MediaStore.ACTION_VIDEO_CAPTURE
            else -> return MediaStore.ACTION_IMAGE_CAPTURE
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if(resultCode == Activity.RESULT_OK) fileUri else null
    }
}