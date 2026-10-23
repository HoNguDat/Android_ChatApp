package com.example.chatapp.data.model

data class ChatConversation(
    val otherUser: User = User(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val lastSenderId: String = "",
    val isRead: Boolean = true
)