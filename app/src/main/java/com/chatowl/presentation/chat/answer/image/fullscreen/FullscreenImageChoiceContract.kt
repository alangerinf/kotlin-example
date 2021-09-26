package com.chatowl.presentation.chat.answer.image.fullscreen

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.chatowl.data.entities.chat.BotChoice

class FullscreenImageChoiceContract : ActivityResultContract<ArrayList<BotChoice>, BotChoice?>() {

    companion object {
        const val FULLSCREEN_IMAGE_URL_LIST_KEY = "FULLSCREEN_URL_LIST"
        const val SELECTED_FULLSCREEN_IMAGE_URL_KEY = "SELECTED_FULLSCREEN_URL"
    }

    override fun createIntent(context: Context, fullscreenImageUrlList: ArrayList<BotChoice>): Intent {
        return Intent(context, FullscreenImageChoiceActivity::class.java).apply {
            putParcelableArrayListExtra(FULLSCREEN_IMAGE_URL_LIST_KEY, fullscreenImageUrlList)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): BotChoice? {
        return if(resultCode == RESULT_OK) {
            intent?.getParcelableExtra(SELECTED_FULLSCREEN_IMAGE_URL_KEY) ?: BotChoice()
        } else {
            null
        }
    }
}