package com.example.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.ui.navigation.AppNavigation
import com.example.chatapp.ui.theme.ChatAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatAppTheme {
                AppNavigation()
            }
        }
    }

    private val repository = AuthRepository()

    override fun onStart() {
        super.onStart()
        repository.goOnline()
    }

    override fun onStop() {
        super.onStop()
        repository.goOffline()
    }
}