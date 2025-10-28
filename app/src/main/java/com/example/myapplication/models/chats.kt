package com.example.myapplication.models

import com.google.firebase.database.PropertyName

data class chats(
    @PropertyName("chatPath") var chatPath: String = "",
    @PropertyName("fromUid") var fromUid: String = "",
    @PropertyName("messageContent") var messageContent: String = "",
    @PropertyName("messageId") var messageId: String = "",
    @PropertyName("messageType") var messageType: String = "",
    @PropertyName("timestamp") var timestamp: Long = 0,
    @PropertyName("toUid") var toUid: String = "",
    var recepteur: String = ""
)