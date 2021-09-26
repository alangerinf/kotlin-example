package com.chatowl.presentation.toolbox.mediaexercise.fullscreen

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.updatePaddingRelative
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chatowl.R
import com.chatowl.presentation.GlideApp
import com.chatowl.presentation.GlideOptions.bitmapTransform
import com.chatowl.presentation.common.extensions.doOnApplyWindowInsets
import com.chatowl.presentation.common.utils.FileUtils.Companion.fileExists
import com.chatowl.presentation.main.MainViewModel
import com.chatowl.presentation.toolbox.mediaexercise.FullscreenPlayerContract.Companion.POSITION
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.SupportRSBlurTransformation
import kotlinx.android.synthetic.main.activity_fullscreen_player.*
import kotlinx.android.synthetic.main.layout_exo_player_controls.*
import kotlinx.android.synthetic.main.row_chat_image_received.view.*


class FullscreenPlayerActivity : AppCompatActivity(), Player.EventListener {

    private var mExoPlayer: SimpleExoPlayer? = null
    private var mExoPlayerCurrentPosition: Long = 0
    private var mCurrentUri: Uri? = null

    lateinit var viewModel: FullScreenPlayerViewModel
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_player)

        hideSystemUiFullScreen()

        activity_fullscreen_player_playerview.doOnApplyWindowInsets { v, windowInsets, padding ->
            v.updatePaddingRelative(bottom = padding.bottom + windowInsets.systemWindowInsetBottom)
        }

        viewModel = ViewModelProvider(
            this,
            FullScreenPlayerViewModel.Factory(application, intent.extras)
        ).get(
            FullScreenPlayerViewModel::class.java
        )

        viewModel.fixedLandscape.observe(this, { fixedLandscape ->
            if (fixedLandscape) requestedOrientation = SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        })

        viewModel.title.observe(this, { title ->
            layout_exo_player_controls_text_view_header.text = title
        })

        viewModel.mediaUri.observe(this, { mediaUri ->
            initializePlayerWithUri(mediaUri)
        })

        viewModel.backgroundUrl.observe(this, { backgroundUrl ->
            GlideApp.with(this).asDrawable()
                .load(backgroundUrl)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        activity_fullscreen_player_playerview.defaultArtwork = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        })

        layout_exo_player_controls_image_view_close.setOnClickListener {
            finishWithPosition(mExoPlayer?.currentPosition?.div(1000) ?: 0)
        }

        iv_close.setOnClickListener {
            finishWithPosition()
        }

        checkInternetPermission()

        mainViewModel.isNetworkAvailable.observe(this, { isNetworkAvailable ->
            val fileExists = fileExists(mCurrentUri)
            when (fileExists || isNetworkAvailable) {
                true -> {
                    iv_no_internet.visibility = View.GONE
                    tv_no_internet.visibility = View.GONE
                    iv_close.visibility = View.GONE
                    activity_fullscreen_player_playerview.visibility = View.VISIBLE
                    iv_background.visibility = View.GONE
                }
                false -> {
                    iv_no_internet.visibility = View.VISIBLE
                    tv_no_internet.visibility = View.VISIBLE
                    iv_close.visibility = View.VISIBLE
                    activity_fullscreen_player_playerview.visibility = View.GONE
                    iv_background.visibility = View.VISIBLE
                    GlideApp.with(this)
                        .load(R.drawable.background_media_player)
                        .centerCrop()
                        .apply(bitmapTransform(BlurTransformation(6, 3)))
                        .into(iv_background)

                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        mainViewModel.connectivityCheckRequested()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // PERMISSIONS
    private fun checkInternetPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            checkInternetPermission()
        }
    }

    // PLAYER CALLBACKS
    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {}
    override fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
    }

    override fun onLoadingChanged(isLoading: Boolean) {}
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                if (viewModel.startPosition > 0) {
                    mExoPlayer?.seekTo(viewModel.startPosition)
                    mExoPlayer?.playWhenReady = true
                    viewModel.resetStartPosition()
                }
            }
            Player.STATE_ENDED -> {
                finishWithPosition(Long.MAX_VALUE)
            }
            Player.STATE_BUFFERING -> {
            }
            Player.STATE_IDLE -> {
            }
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {}
    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
    override fun onPlayerError(error: ExoPlaybackException) {
        error.printStackTrace()
    }

    override fun onPositionDiscontinuity(reason: Int) {}
    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
    override fun onSeekProcessed() {}

    // PLAYER
    private fun initializePlayerWithUri(uri: Uri) {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            val videoTrackSelectionFactory: TrackSelection.Factory =
                AdaptiveTrackSelection.Factory()
            val trackSelector: TrackSelector = DefaultTrackSelector(
                this,
                videoTrackSelectionFactory
            )
            mExoPlayer = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
            mExoPlayer?.addListener(this)
            activity_fullscreen_player_playerview.player = mExoPlayer
        }
        loadUriIntoPlayer(uri)
    }

    private fun loadUriIntoPlayer(mediaUri: Uri?) {
        val userAgent = Util.getUserAgent(this, getString(R.string.app_name))
        val dataSourceFactory = DefaultDataSourceFactory(
            this, userAgent, DefaultBandwidthMeter.Builder(
                this
            ).build()
        )
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
            mediaUri
        )
        mExoPlayer?.prepare(mediaSource)
        mExoPlayer?.playWhenReady = true
        mCurrentUri = mediaUri
    }

    private fun releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayerCurrentPosition = mExoPlayer!!.currentPosition
            mExoPlayer!!.stop()
            mExoPlayer!!.release()
            mExoPlayer = null
        }
    }

    public override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    public override fun onResume() {
        super.onResume()
        mCurrentUri?.let { uri ->
            initializePlayerWithUri(uri)
            if (mExoPlayerCurrentPosition > 0) mExoPlayer!!.seekTo(mExoPlayerCurrentPosition)
        }
    }

    private fun hideSystemUiFullScreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onBackPressed() {
        finishWithPosition(mExoPlayer?.currentPosition?.div(1000) ?: 0)
    }

    private fun finishWithPosition(position: Long = -1) {
        var currentPosition = position
        if (currentPosition > -1) {
            mExoPlayer?.duration?.let{
                if (currentPosition >= (it / 1000) - 1){
                    currentPosition = Long.MAX_VALUE
                }
            }
        }
        val intent = Intent()
        intent.putExtra(POSITION, currentPosition)
        setResult(RESULT_OK, intent)
        finish()
    }
}