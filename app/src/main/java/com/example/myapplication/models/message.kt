package com.example.myapplication.models

import com.google.firebase.database.PropertyName

data class message(
    @PropertyName("chatPath") val chatPath: String = "",
    @PropertyName("fromUid") val fromUid: String = "",
    @PropertyName("messageContent") val messageContent: String = "",
    @PropertyName("messageId") val messageId: String = "",
    @PropertyName("messageType") val messageType: String = "",
    @PropertyName("timestamp") val timestamp: Long = 0,
    @PropertyName("toUid") val toUid: String = ""
)