package com.chatowl.data.api.chat

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.chatowl.presentation.common.application.ChatOwlApplication
import java.lang.Exception

class PlayListPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private val playList: MutableList<String> = arrayListOf()
    private var isMuted: Boolean = false

    init {
        reset()
    }

    fun setMute(isMuted: Boolean) {
        this.isMuted = isMuted
        if (isMuted) reset()
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            try {
                mediaPlayer?.pause()
            } catch (e: Exception) {
                Log.e(this::class.java.simpleName, "We couldn't pause")
            }

        }
    }

    fun resume() {
        if (mediaPlayer?.isPlaying == false) {
            try {
                mediaPlayer?.start()
            } catch (e: Exception) {
                Log.e(this::class.java.simpleName, "We couldn't resume")
            }
        }
    }

    private fun reset() {
        playList.clear()
        if (mediaPlayer?.isPlaying == true) {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.seekTo(0)
            } catch (e: Exception) {
                Log.e(this::class.java.simpleName, "We couldn't reset")
            }

        }
        mediaPlayer = null
    }

    companion object {

        private var INSTANCE: PlayListPlayer? = null

        fun getInstance(): PlayListPlayer {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = PlayListPlayer()
                    INSTANCE = instance
                }
                return instance
            }
        }

        fun resetInstance() {
            INSTANCE = null
        }
    }

    fun addItem(audio: String) {
        if (!isMuted) {
            playList.add(audio)
            if (mediaPlayer == null || mediaPlayer?.isPlaying == false) {
                playNextOne()
            }
        }
    }

    fun addItems(audio: List<String>) {
        if (!isMuted) {
            playList.addAll(audio)
            if (mediaPlayer == null || mediaPlayer?.isPlaying == false) {
                playNextOne()
            }
        }
    }

    private fun playNextOne() {
        if (playList.isNotEmpty()) {
            playAudio(playList.first())
        }
    }

    private fun playAudio(audio: String) {
        Log.d(this::class.java.simpleName, "play $audio")
        try {
            mediaPlayer =
                MediaPlayer.create(ChatOwlApplication.get().applicationContext, Uri.parse(audio))
        } catch (e: Exception) {
            Log.e(this::class.java.simpleName, "We couldn't create the player of $audio")
        }

        mediaPlayer?.setOnPreparedListener {
            try {
                mediaPlayer?.start()
            } catch (e: Exception) {
                Log.e(this::class.java.simpleName, "We couldn't play $audio")
            }
        }
        mediaPlayer?.setOnCompletionListener {
            playList.remove(audio)
            playNextOne()
        }
    }

}