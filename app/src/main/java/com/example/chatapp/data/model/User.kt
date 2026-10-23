package com.example.chatapp.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val status: String = "OFFLINE",
    val nameLowercase: String = "",
    val phone: String = "",
)
