package com.chatowl.data.helpers

import com.chatowl.data.entities.chat.ChatHistoryResponse
import com.chatowl.data.entities.toolbox.ToolboxExercise
import com.chatowl.data.entities.toolbox.categorydetail.ToolboxCategoryDetail
import com.chatowl.data.entities.toolbox.home.ToolboxHome
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.io.IOException
import java.nio.charset.StandardCharsets

object MockData {

    fun getChatItems(): ChatHistoryResponse {
        return mockData(ChatHistoryResponse::class.java, "chat_history_response.json") ?: ChatHistoryResponse(emptyList())
    }

    fun getToolboxCategory(): ToolboxCategoryDetail? {
        return mockData(ToolboxCategoryDetail::class.java, "toolbox_category.json")
    }

    fun getToolboxHome(): ToolboxHome? {
        return mockData(ToolboxHome::class.java, "toolbox_home.json")
    }

    fun getExerciseDetail(): ToolboxExercise? {
        return mockData(ToolboxExercise::class.java, "toolbox_exercise.json")
    }

    private fun <T>mockData(dataClass: Class<T>, source: String): T? {
        val jsonString = loadJSONFromAsset(source)
        val moshiAdapter: JsonAdapter<T> =
                Moshi.Builder().build().adapter(dataClass)
        val activities = moshiAdapter.fromJson(jsonString)
        return activities
    }

    private fun loadJSONFromAsset(service: String): String {
        val context = ChatOwlApplication.get()
        val json: String
        json = try {
            val `is` = context.assets.open(service)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer, StandardCharsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return ""
        }
        return json
    }

}