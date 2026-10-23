package com.example.chatapp.data.model

data class ChatStatus(
    val chatRoomId: String = "",
    val initiatorId: String = "", // Người gửi tin nhắn đầu tiên
    val receiverId: String = "",  // Người nhận (người có quyền Chấp nhận / Từ chối)
    val status: String = "ACCEPTED" // PENDING, ACCEPTED, DECLINED
)