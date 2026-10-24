package com.example.chatapp.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.model.ChatStatus
import com.example.chatapp.data.model.Message
import com.example.chatapp.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
class ChatViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _chatStatus = MutableStateFlow<ChatStatus?>(null)
    val chatStatus: StateFlow<ChatStatus?> = _chatStatus.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    val currentUserId: String? get() = repository.currentUserId
    private var listenJob: Job? = null
    private var statusJob: Job? = null

    fun loadMessages(receiverId: String) {
        listenJob?.cancel()
        statusJob?.cancel()

        viewModelScope.launch {
            repository.markMessagesAsRead(receiverId)
        }

        // Lắng nghe danh sách tin nhắn
        listenJob = viewModelScope.launch {
            repository.getMessages(receiverId).collect { msgList ->
                _messages.value = msgList
            }
        }

        // Lắng nghe trạng thái PENDING / ACCEPTED / DECLINED
        statusJob = viewModelScope.launch {
            repository.getChatStatus(receiverId).collect { status ->
                _chatStatus.value = status
            }
        }
    }

    fun acceptChat(receiverId: String) {
        viewModelScope.launch {
            repository.updateChatStatus(receiverId, "ACCEPTED")
        }
    }

    fun declineChat(receiverId: String) {
        viewModelScope.launch {
            repository.updateChatStatus(receiverId, "DECLINED")
        }
    }

    fun sendMessage(receiverId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMediaMessage(receiverId, text = text.trim())
        }
    }

    // Xử lý upload ảnh và gửi tin nhắn hình ảnh
    fun sendImageMessage(context: Context, receiverId: String, imageUri: Uri) {
        viewModelScope.launch {
            _isUploading.value = true
            val base64Image = repository.convertUriToBase64(context, imageUri)
            if (base64Image != null) {
                repository.sendMediaMessage(receiverId, imageUrl = base64Image)
            }
            _isUploading.value = false
        }
    }
}