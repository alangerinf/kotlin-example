package com.chatowl.presentation.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.toolbox.MoodMeterMode
import com.chatowl.data.entities.toolbox.ToolboxMoodMeter
import com.chatowl.databinding.FragmentMoodMeterItemBinding

class MoodMeterAdapter(
    private val context: Context,
    private val onChatItemClick: (item: ToolboxMoodMeter) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var itemList = emptyList<ToolboxMoodMeter>().toMutableList()

    fun setList(toolboxMoodMeterList: MutableList<ToolboxMoodMeter>){
        this.itemList = toolboxMoodMeterList
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val dataBinding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            R.layout.fragment_mood_meter_item,
            parent,
            false
        )
        return CustomViewHolder(dataBinding)
    }

    class CustomViewHolder(binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        var mDataBinding: FragmentMoodMeterItemBinding = binding as FragmentMoodMeterItemBinding
    }

    @SuppressLint("SetTextI18n")
    @ExperimentalStdlibApi
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        val binding = (holder as CustomViewHolder).mDataBinding
        binding.root.setOnClickListener {
            onChatItemClick.invoke(item)
        }
        binding.tViewTitle.text = item.moodLabel
        binding.tViewSubTitle.text =
            (item.intensities.getOrNull(0)?:"") +
                    if(item.intensities.getOrNull(1)!=null) {
                        " - "+ item.intensities[1]
                    } else ""
        binding.tViewInput.text = item.inputs.toString()
        when (item.getMode()) {
            MoodMeterMode.WORRY, MoodMeterMode.HOPE -> setViewColor(binding, ContextCompat.getColor(context, R.color.emotion_hope))
            MoodMeterMode.ANGER -> setViewColor(binding, ContextCompat.getColor(context, R.color.emotion_anger))
            MoodMeterMode.LONELINESS, MoodMeterMode.DEPRESSION -> setViewColor(binding, ContextCompat.getColor(context, R.color.emotion_loneliness))
            MoodMeterMode.NERVOUSNESS -> setViewColor(binding, ContextCompat.getColor(context, R.color.emotion_nervousness))
            MoodMeterMode.ANXIETY -> setViewColor(binding, ContextCompat.getColor(context, R.color.emotion_anxiety))
            MoodMeterMode.OTHER -> setViewColor(binding, ContextCompat.getColor(context, R.color.emotion_anger))
        }
    }

    private fun setViewColor(binding: FragmentMoodMeterItemBinding, color: Int) {
        binding.tViewTitle.setTextColor(color)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}


