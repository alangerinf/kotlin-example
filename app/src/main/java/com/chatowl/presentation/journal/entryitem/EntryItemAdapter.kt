package com.chatowl.presentation.journal.entryitem

import android.content.Context
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.journal.EntryItem
import com.chatowl.data.entities.journal.EntryItemType
import com.chatowl.presentation.GlideApp
import com.chatowl.presentation.common.extensions.*
import com.chatowl.presentation.common.utils.FileUtils
import com.chatowl.presentation.journal.entryitem.EntryAdapterItem.MediaEntryAdapterItem
import com.chatowl.presentation.journal.entryitem.EntryAdapterItem.TextEntryAdapterItem
import com.google.android.material.internal.TextWatcherAdapter
import kotlinx.android.synthetic.main.row_entry_item_audio.view.*
import kotlinx.android.synthetic.main.row_entry_item_image.view.*
import kotlinx.android.synthetic.main.row_entry_item_text.view.*
import kotlinx.android.synthetic.main.row_entry_item_video.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class EntryItemAdapter(
    private val fragment: Fragment,
    private val itemClick: (EntryAdapterItem) -> Unit,
    private val deleteClick: (Int) -> Unit,
    private val openClick: (EntryAdapterItem) -> Unit,
    private val onTextChanged: (Int, Int, String) -> Unit,
    initEditMode: Boolean
) : ListAdapter<EntryAdapterItem, RecyclerView.ViewHolder>(
    JournalAdapterItemDiffCallback()
), Handler.Callback {

    private val TAG = this::class.java.simpleName


    private lateinit var fileUtils: FileUtils

    private var isEdit: Boolean = initEditMode

    private var mediaPlayer: MediaPlayer? = null
    private var playingPosition: Int = -1
    private var playingViewHolder: AudioEntryViewHolder? = null

    private val handler = Handler(Looper.getMainLooper(), this)

    companion object {
        const val UPDATE_SEEK_BAR = 1
    }

    private val itemClickedAt: (Int) -> Unit = { position ->
        itemClick.invoke(getItem(position))
    }

    private val itemDeleteAt: (Int) -> Unit = { position ->
        deleteClick.invoke(position)
    }

    private val itemOpenAt: (Int) -> Unit = { position ->
        openClick.invoke(getItem(position))
    }

    private val textItemChangedAt: (Int, String) -> Unit = { position, newText ->
        val item = getItem(position) as TextEntryAdapterItem
        item.content = newText
        onTextChanged.invoke(position, item.itemId, item.content)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            EntryItemType.TEXT.ordinal -> TextEntryViewHolder(
                inflater.inflate(
                    R.layout.row_entry_item_text,
                    parent,
                    false
                ), itemClickedAt
            )
            EntryItemType.AUDIO.ordinal -> AudioEntryViewHolder(
                inflater.inflate(
                    R.layout.row_entry_item_audio,
                    parent,
                    false
                ), itemClickedAt
            )
            EntryItemType.VIDEO.ordinal -> VideoEntryViewHolder(
                inflater.inflate(
                    R.layout.row_entry_item_video,
                    parent,
                    false
                ), itemClickedAt
            )
            EntryItemType.IMAGE.ordinal -> ImageEntryViewHolder(
                inflater.inflate(
                    R.layout.row_entry_item_image,
                    parent,
                    false
                ), itemClickedAt
            )
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is TextEntryViewHolder -> holder.bind(item as TextEntryAdapterItem)
            is AudioEntryViewHolder -> holder.bind(item as MediaEntryAdapterItem, position)
            is ImageEntryViewHolder -> holder.bind(item as MediaEntryAdapterItem)
            is VideoEntryViewHolder -> holder.bind(item as MediaEntryAdapterItem)
        }
    }

    inner class TextEntryViewHolder(
        itemView: View,
        private val itemClick: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(
        itemView
    ) {
        init {
            itemView.setOnClickListener {
                itemClickedAt.invoke(adapterPosition)
            }
        }

        fun bind(item: TextEntryAdapterItem) {
            itemView.row_entry_item_text_edit_text.setText(item.content)
            itemView.row_entry_item_text_edit_text.setSelection(item.content.length)
            itemView.row_entry_item_text_edit_text.addTextChangedListener(object :
                TextWatcherAdapter() {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    textItemChangedAt(adapterPosition, s.toString())
//                    (getItem(adapterPosition) as TextEntryAdapterItem).content = s.toString()
//                    onTextChanged(item.id, s.toString())
                }
            })
            itemView.row_entry_item_text_edit_text.isFocusableInTouchMode = isEdit
        }
    }

    inner class ImageEntryViewHolder(
        itemView: View,
        private val itemClick: (position: Int) -> Unit
    ) :
        RecyclerView.ViewHolder(
            itemView
        ) {

        init {
            itemView.setOnClickListener {
                itemClickedAt.invoke(adapterPosition)
            }

            itemView.row_entry_item_image_image_view_delete.setOnClickListener {
                itemDeleteAt.invoke(adapterPosition)
            }

            itemView.row_entry_item_image_image_view_fullscreen.setOnClickListener {
                itemOpenAt.invoke(adapterPosition)
            }
        }

        fun bind(item: MediaEntryAdapterItem) {

            GlideApp.with(itemView).apply {
                item.uri?.let {
                    load(it).into(itemView.row_entry_item_image_image_view_background)
                } ?: let {
                    load(item.mediaUrl).into(itemView.row_entry_item_image_image_view_background)
                }
            }

            itemView.row_entry_item_image_image_view_fullscreen.visibility =
                if (isEdit) View.GONE else View.VISIBLE
            itemView.row_entry_item_image_image_view_delete.visibility =
                if (isEdit) View.VISIBLE else View.GONE
        }
    }

    inner class VideoEntryViewHolder(
        itemView: View,
        private val itemClick: (position: Int) -> Unit
    ) :
        RecyclerView.ViewHolder(
            itemView
        ) {
        init {
            itemView.setOnClickListener {
                itemClickedAt.invoke(adapterPosition)
            }

            itemView.row_entry_item_video_image_view_delete.setOnClickListener {
                itemDeleteAt.invoke(adapterPosition)
            }

            itemView.row_entry_item_video_image_button_play.setOnClickListener {
                itemOpenAt.invoke(adapterPosition)
            }
        }

        fun bind(item: MediaEntryAdapterItem) {
            GlideApp.with(itemView.context).apply {
                item.uri?.let {
                    load(it).into(itemView.row_entry_item_video_image_view_background)
                } ?: let {
                    load(item.mediaUrl).into(itemView.row_entry_item_video_image_view_background)
                }
            }
            itemView.row_entry_item_video_image_view_delete.visibility =
                if (isEdit) View.VISIBLE else View.GONE
        }
    }

    inner class AudioEntryViewHolder(
        itemView: View,
        private val itemClick: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(
        itemView
    ) {

        init {
            itemView.setOnClickListener {
                itemClickedAt.invoke(adapterPosition)
            }
        }

        private fun getURL(fullURL: String): String {
            return fullURL.substring(fullURL.indexOf("://") + 3, fullURL.indexOf(".com") + 4)
        }

        fun bind(item: MediaEntryAdapterItem, position: Int) {

            itemView.row_entry_item_audio_delete.visibility =
                if (isEdit) View.VISIBLE else View.GONE

            itemView.row_entry_item_audio_button_play.setOnClickListener {
                onPlayPauseClicked(adapterPosition, item.uri, item.mediaUrl)
            }

            itemView.row_entry_item_audio_delete.setOnClickListener {
                onDeleteClicked(adapterPosition)
            }

            itemView.row_entry_item_audio_image_button_fast_forward.setOnClickListener {
                onFastForwardClicked()
            }

            itemView.row_entry_item_audio_image_button_rewind.setOnClickListener {
                onRewindClicked()
            }

            itemView.row_journal_entry_audio_slider.addOnChangeListener { slider, value, fromUser ->
                if (fromUser) {
                    mediaPlayer?.seekTo(slider.value.toInt())
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                /*try {
                    val address = getURL(BuildConfig.BASE_URL) //BuildConfig.BASE_URL
                    val ipAddr: InetAddress = InetAddress.getByName(address)
                    !ipAddr.equals("")*/
                val retriever = MediaMetadataRetriever()
                itemView.customEnable()
                val timeString: String = try {
                    (item.uri!!.toFile().getMediaDuration(fragment.requireContext()) ?: "0")
                } catch (e: Exception) {
                    Log.e(TAG, "111+" + e.toString())
                    try {
                        retriever.setDataSource(item.mediaUrl)
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?: "0"
                    } catch (e2: Exception) {
                        Log.e(TAG, "222+" + e2.toString())
                        "0"
                    }
                }.toLong().div(1000F).toInt().secondsToMinuteSecondsString()
                withContext(Dispatchers.Main) {
                    itemView.row_journal_entry_audio_text_view_time_remaining.text =
                        timeString
                    if (position == playingPosition) {
                        playingViewHolder = this@AudioEntryViewHolder
                        updatePlayingView()
                    } else {
                        updateNonPlayingView(this@AudioEntryViewHolder)
                    }
                    if (timeString == "0") {
                        itemView.customDisable()
                        itemView.row_journal_entry_audio_slider.isEnabled = false
                        itemView.row_entry_item_audio_button_play.customDisable()
                        itemView.row_entry_item_audio_image_button_rewind.customDisable()
                        itemView.row_entry_item_audio_image_button_fast_forward.customDisable()
                        itemView.row_entry_item_audio_delete.customDisable()
                    }
                    Log.e(TAG, "data dura -> " + timeString)
                }
            }
        }




        fun File.getMediaDuration(context: Context): String? {
            fileUtils = FileUtils(context, context.getString(R.string.journal))
            if (!exists()) return "0"
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(context, fileUtils.getFileUri(file = this));
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration
            mediaPlayer.release();
            return duration.toString()
        }

        private fun onPlayPauseClicked(adapterPosition: Int, localURI: Uri?, mediaUrl: String?) {
            val uriFileExist = localURI?.toFile()?.exists() ?: false
            if (uriFileExist || !mediaUrl.isNullOrEmpty()) {
                if (adapterPosition == playingPosition) {
                    // toggle between play/pause of audio
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.pause()
                    } else {
                        mediaPlayer?.start()
                    }
                } else {
                    // start another audio playback
                    playingPosition = adapterPosition
                    if (mediaPlayer != null) {
                        if (null != playingViewHolder) {
                            updateNonPlayingView(playingViewHolder)
                        }
                        mediaPlayer?.release()
                    }
                    playingViewHolder = this
                    startMediaPlayer(itemView.context, localURI, mediaUrl)
                }
                updatePlayingView()
            }
        }

        private fun onFastForwardClicked() {
            if (adapterPosition == playingPosition && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.let { player ->
                    val position = (player.currentPosition + 15000).coerceAtMost(player.duration)
                    player.seekTo(position)
                }
            }
        }

        private fun onDeleteClicked(adapterPosition: Int) {
            mediaPlayer?.pause()
            itemDeleteAt.invoke(adapterPosition)
        }

        private fun onRewindClicked() {
            if (adapterPosition == playingPosition && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.let { player ->
                    val position = (player.currentPosition - 15000).coerceAtLeast(0)
                    player.seekTo(position)
                }
            }
        }

        fun updateSeekBar() {
            itemView.row_journal_entry_audio_slider?.value =
                mediaPlayer?.currentPosition?.toFloat() ?: 0F
            itemView.row_journal_entry_audio_text_view_time_current?.text =
                mediaPlayer?.currentPosition?.div(1000)?.secondsToMinuteSecondsString()
        }

    }

    private fun updatePlayingView() {
        val valueTo = mediaPlayer?.duration?.toFloat() ?: 0F

        val currentValue = mediaPlayer?.currentPosition?.toFloat() ?: 0F

        playingViewHolder?.itemView?.row_journal_entry_audio_slider?.valueTo = valueTo
        playingViewHolder?.itemView?.row_journal_entry_audio_slider?.value = currentValue
        playingViewHolder?.itemView?.row_journal_entry_audio_slider?.isEnabled = true

        if (mediaPlayer?.isPlaying == true) {
            playingViewHolder?.itemView?.row_entry_item_audio_button_play
                ?.setImageResource(R.drawable.ic_pause_player)
            handler.sendEmptyMessageDelayed(UPDATE_SEEK_BAR, 1000)
        } else {
            playingViewHolder?.itemView?.row_entry_item_audio_button_play
                ?.setImageResource(R.drawable.ic_play)
            handler.removeMessages(UPDATE_SEEK_BAR)
        }
    }

    private fun updateNonPlayingView(holder: AudioEntryViewHolder?) {
        holder?.itemView?.row_entry_item_audio_button_play?.setImageResource(R.drawable.ic_play)
    }

    private fun startMediaPlayer(context: Context, localURI: Uri?, mediaUrl: String?) {
        fileUtils = FileUtils(context, context.getString(R.string.journal))
        val uriFileExist = localURI?.toFile()?.exists() ?: false
        try {
            if (uriFileExist) {
                mediaPlayer = MediaPlayer.create(
                    context,
                    fileUtils.getFileUri(localURI!!.toFile())
                )
            } else {
                mediaPlayer = MediaPlayer.create(context, Uri.parse(mediaUrl))
            }
            mediaPlayer?.setOnCompletionListener { releaseMediaPlayer() }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseMediaPlayer() {
        playingViewHolder?.let {
            updateNonPlayingView(it)
        }
        mediaPlayer?.release()
        mediaPlayer = null
        playingPosition = -1
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        // commented this out cause didn't understand the purpose of it
//        if (playingPosition == holder.adapterPosition) {
//            updateNonPlayingView(holder as AudioEntryViewHolder)
//            playingViewHolder = null
//        }
        playingViewHolder = null
    }

    fun setEditMode(isEdit: Boolean) {
        this.isEdit = isEdit
        notifyDataSetChanged()
    }

    override fun handleMessage(message: Message): Boolean {
        if (message.what == UPDATE_SEEK_BAR) {
            playingViewHolder?.updateSeekBar()
            handler.sendEmptyMessageDelayed(UPDATE_SEEK_BAR, 1000)
            return true
        }
        return false
    }
}

class JournalAdapterItemDiffCallback : DiffUtil.ItemCallback<EntryAdapterItem>() {
    override fun areItemsTheSame(
        oldAdapterItem: EntryAdapterItem,
        newAdapterItem: EntryAdapterItem
    ): Boolean {
        return oldAdapterItem.id == newAdapterItem.id
    }

    override fun areContentsTheSame(
        oldAdapterItem: EntryAdapterItem,
        newAdapterItem: EntryAdapterItem
    ): Boolean {
        return oldAdapterItem == newAdapterItem
    }
}

sealed class EntryAdapterItem {

    data class TextEntryAdapterItem(val itemId: Int, val itemType: Int, var content: String) :
        EntryAdapterItem() {
        override val id = itemId
        override val type = itemType

        constructor(entryItem: EntryItem) : this(
            entryItem.id,
            EntryItemType.valueOf(entryItem.type.upperCase()).ordinal,
            entryItem.content ?: ""
        )
    }

    data class MediaEntryAdapterItem(
        val itemId: Int,
        val itemType: Int,
        val mediaUrl: String,
        val uri: Uri?
    ) :
        EntryAdapterItem() {
        override val id = itemId
        override val type = itemType

        constructor(entryItem: EntryItem) : this(
            entryItem.id,
            EntryItemType.valueOf(entryItem.type.upperCase()).ordinal,
            entryItem.mediaUrl ?: "",
            entryItem.uri?.toUri()
        )
    }

    abstract val id: Int
    abstract val type: Int
}