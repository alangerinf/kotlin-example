package com.chatowl.presentation.settings.crisissupport

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chatowl.data.entities.crisissupport.CrisisSupportItem
import com.chatowl.data.entities.crisissupport.FullCrisisSupportResponse
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.delay
import java.io.IOException
import java.nio.charset.StandardCharsets


class CrisisSupportViewModel(application: Application) : BaseViewModel(application) {

    val errorMessage: LiveData<Int> get() = _errorMessage
    private val _errorMessage = SingleLiveEvent<Int>()

    val crisisSupportItemList: LiveData<List<CrisisSupportItem>> get() = _crisisSupportItemList
    private val _crisisSupportItemList = MutableLiveData<List<CrisisSupportItem>>()

    private val _callIntent = SingleLiveEvent<Intent>()
    val callIntent: LiveData<Intent> get() = _callIntent

    init {
        fetchItems()
    }

    private fun fetchItems() {
        _isLoading.value = true
        viewModelScope.fetch({
            delay(1500)
        }, {
            _crisisSupportItemList.value = mockData()?.data
            _isLoading.value = false
        }, {
            _isLoading.value = false
        })
    }

    private fun mockData(): FullCrisisSupportResponse? {
        val trips = loadJSONFromAsset("crisis_support_response.json")

        val moshiAdapter: JsonAdapter<FullCrisisSupportResponse> =
            Moshi.Builder().build().adapter(FullCrisisSupportResponse::class.java)
        val activities = moshiAdapter.fromJson(trips)
        return activities
    }

    private fun loadJSONFromAsset(assetName: String): String {

        val context = ChatOwlApplication.get()

        val json: String
        json = try {
            val `is` = context.assets.open(assetName)
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

    fun onItemClick(item: CrisisSupportItem) {
        val intent = Intent()
        intent.action = Intent.ACTION_DIAL
        intent.data = Uri.parse("tel:${item.number}")
        _callIntent.value = intent
    }

}
