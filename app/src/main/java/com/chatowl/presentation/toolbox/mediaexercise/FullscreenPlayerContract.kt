package com.chatowl.presentation.toolbox.mediaexercise

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.chatowl.data.entities.toolbox.fullscreen.FullscreenPlayerData
import com.chatowl.presentation.toolbox.mediaexercise.fullscreen.FullscreenPlayerActivity

class FullscreenPlayerContract : ActivityResultContract<FullscreenPlayerData, Long>() {

    companion object {
        const val FULLSCREEN_PLAYER_DATA_KEY = "FULLSCREEN_PLAYER_DATA"
        const val POSITION = "POSITION"
    }

    override fun createIntent(context: Context, fullscreenPlayerData: FullscreenPlayerData): Intent {
        return Intent(context, FullscreenPlayerActivity::class.java).apply {
            putExtra(FULLSCREEN_PLAYER_DATA_KEY, fullscreenPlayerData)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Long {
        return intent?.getLongExtra(POSITION, 0) ?: 0
    }
}