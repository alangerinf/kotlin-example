package com.chatowl.presentation.moodmeter

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.chatowl.R
import com.chatowl.data.entities.toolbox.MoodMeterMode
import com.chatowl.data.entities.toolbox.ToolboxMoodMeter
import com.chatowl.data.entities.tracking.PostEventTrackingRequest
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.databinding.ActivityMoodMeterPickerBinding
import com.chatowl.presentation.common.utils.PreferenceHelper
import com.chatowl.presentation.common.utils.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception


class MoodMeterPickerActivity : AppCompatActivity() {

    private lateinit var viewModel: MoodMeterPickerViewModel

    private lateinit var binding: ActivityMoodMeterPickerBinding
    private var progressValue = 0

    companion object {
        const val EXTRA_KEY_TOOLBOX_MOOD_METER = "MOOD_METER_ITEM"
    }

    fun getScreenName() = "Mood input"

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_mood_meter_picker
        )

        viewModel = ViewModelProvider(this, MoodMeterPickerViewModel.Factory(application))
            .get(MoodMeterPickerViewModel::class.java)

        val item = intent.extras?.getParcelable<ToolboxMoodMeter>(EXTRA_KEY_TOOLBOX_MOOD_METER)!!
        progressValue = 0
        item.intensities.forEachIndexed { index, element ->
            when (index) {
                0 -> binding.tViewLowLevel.text = element
                1 -> binding.tViewHighLevel.text = element
            }
        }
        binding.tViewTitle.text = item.moodLabel

        val (raw, color) = when (item.getMode()) {
            MoodMeterMode.WORRY, MoodMeterMode.HOPE -> Pair(
                R.raw.hopeful_lottie,
                R.color.emotion_hope
            )
            MoodMeterMode.ANGER -> Pair(R.raw.anger_lottie, R.color.emotion_anger)
            MoodMeterMode.LONELINESS, MoodMeterMode.DEPRESSION -> {
                binding.lottie.scaleX = 1.5f
                binding.lottie.scaleY = 1.5f
                Pair(R.raw.loneliness_lottie, R.color.emotion_loneliness)
            }
            MoodMeterMode.NERVOUSNESS -> Pair(R.raw.nervousness_lottie, R.color.emotion_nervousness)
            MoodMeterMode.ANXIETY -> Pair(R.raw.anxiety_lottie, R.color.emotion_anxiety)
            else -> Pair(null, null)
        }

        binding.lottie.apply {
            setAnimation(raw ?: R.raw.anger_lottie)
            pauseAnimation()
            setMinAndMaxProgress(0.5f, 0.5f)
        }

        binding.tViewTitle.setTextColor(ContextCompat.getColor(this, color ?: R.color.white))

        var hexColor = Color.parseColor(resources.getString(color ?: R.color.white))

        val myList = ColorStateList.valueOf(hexColor)

        binding.seekBar.apply {
            progressTintList = myList
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    // TODO Auto-generated method stub
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    // TODO Auto-generated method stub
                }

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    // TODO Auto-generated method stub
                    progressValue = progress - 10

                    binding.lottie.apply {
                        val percentage = (progress * 5) / 100f
                        setMinAndMaxProgress(percentage, percentage)
                        pauseAnimation()
                    }
                }
            })
        }

        binding.tViewToolBarTitle.text = "Recovery Meter"
        binding.iBtnBack.setOnClickListener {
            onBackPressed()
        }
        binding.btnSubmit.setOnClickListener {
            viewModel.sendMood(item.moodId, progressValue).observe(this, {
                if (it) setResult(Activity.RESULT_OK) else setResult(Activity.RESULT_CANCELED)
                onBackPressed()
            })
        }
        val accessToken =
            PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.ACCESS_TOKEN, "")
                ?: ""
        if (accessToken.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {

                Log.i("ViewModelFragment", getScreenName())
                try {
                    ChatOwlUserRepository.sendTracking(
                        PostEventTrackingRequest.ViewedScreenBuilder()
                            .screenName(getScreenName())
                            .toolboxCategoryName("")
                            .therapyPlanName("")
                            .exerciseName("")
                            .build()
                    )
                } catch (e: Exception) {
                    Log.e("ViewModelFragment", e.toString())
                }
            }
        }
    }

}