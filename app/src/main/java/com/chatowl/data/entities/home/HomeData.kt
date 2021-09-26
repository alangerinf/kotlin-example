package com.chatowl.data.entities.home

data class HomeData(
        var clientInfo: ClientInformation = ClientInformation(),
        var activitiesToday: List<ProgramActivity> = emptyList(),
        var activitiesSuggested: List<ProgramActivity> = emptyList()
) {
    fun isEmpty(): Boolean {
        return this.activitiesToday.isEmpty() && this.activitiesSuggested.isEmpty()
    }
}