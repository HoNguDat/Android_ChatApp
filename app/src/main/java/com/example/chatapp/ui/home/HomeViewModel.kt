package com.example.chatapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.model.ChatConversation
import com.example.chatapp.data.model.User
import com.example.chatapp.data.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.onStart
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    // Lắng nghe Real-Time người dùng & tin nhắn mới nhất
    val conversations: StateFlow<List<ChatConversation>> = combine(
        repository.getUsers(),
        repository.getRecentConversations().onStart { emit(emptyList()) },
        searchQuery.debounce(300).distinctUntilChanged()
    ) { users, recentChats, query ->
        val cleanQuery = query.trim()

        // 1. Lọc danh sách user theo số điện thoại (nếu ô tìm kiếm có nội dung)
        val filteredUsers = if (cleanQuery.isBlank()) {
            users
        } else {
            users.filter { user ->
                user.phone.contains(cleanQuery)
            }
        }

        // 2. Map người dùng với lịch sử trò chuyện gần nhất (nếu có)
        val conversationList = filteredUsers.map { user ->
            val recent = recentChats.find { it.otherUser.uid == user.uid }
            if (recent != null) {
                // Đã có lịch sử trò chuyện -> Lấy thông tin tin nhắn cuối cùng thực tế
                recent.copy(otherUser = user)
            } else {
                // Chưa từng nhắn tin -> Để thông báo mặc định
                ChatConversation(
                    otherUser = user,
                    lastMessage = "Chạm để bắt đầu trò chuyện",
                    lastTimestamp = 0L,
                    lastSenderId = "",
                    isRead = true
                )
            }
        }

        // 3. Sắp xếp danh sách: Cuộc trò chuyện nào có tin nhắn mới hơn sẽ nằm ở trên cùng
        conversationList.sortedByDescending { it.lastTimestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Thông tin người dùng hiện tại
    val currentUser: StateFlow<User?> = repository.getCurrentUserFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Trạng thái Edit Profile
    val isEditing = MutableStateFlow(false)
    val editName = MutableStateFlow("")
    val editPhone = MutableStateFlow("")

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun startEditing() {
        editName.value = currentUser.value?.name ?: ""
        editPhone.value = currentUser.value?.phone ?: ""
        isEditing.value = true
    }

    fun onEditNameChanged(newName: String) {
        editName.value = newName
    }

    fun onEditPhoneChanged(newPhone: String) {
        editPhone.value = newPhone
    }

    fun cancelEditing() {
        isEditing.value = false
    }

    fun saveProfile() {
        val newName = editName.value.trim()
        val newPhone = editPhone.value.trim()
        if (newName.isBlank()) return

        viewModelScope.launch {
            repository.updateUserProfile(newName, newPhone)
            isEditing.value = false
        }
    }

    fun logout() {
        repository.logout()
    }
}