package com.chatowl.data.entities.toolbox.home

import com.chatowl.data.entities.toolbox.ToolboxCategoryType
import com.chatowl.data.entities.toolbox.ToolboxItem
import com.chatowl.presentation.toolbox.home.ITEM_VIEW_TYPE_SHOW_MORE
import com.chatowl.presentation.toolbox.home.ITEM_VIEW_TYPE_TOOLBOX_ITEM

data class ToolboxHomeGroup(
    var id: Int,
    var title: String = "",
    var subtitle: String = "",
    var groupName: String = "",
    var groupType: ToolboxCategoryType,
    var items: List<ToolboxHomeGroupItem>,
    var pausedAt: String? = null
)

sealed class ToolboxHomeGroupItem {

    data class ToolboxHomeToolboxItem(val toolboxItem: ToolboxItem, val parentName: String) : ToolboxHomeGroupItem() {
        override val id = toolboxItem.id
        override val type = ITEM_VIEW_TYPE_TOOLBOX_ITEM
    }

    data class ToolboxHomeShowMore(val itemId: Int = -1) : ToolboxHomeGroupItem() {
        override val id = itemId
        override val type = ITEM_VIEW_TYPE_SHOW_MORE
    }

    abstract val id: Int
    abstract val type: Int
}