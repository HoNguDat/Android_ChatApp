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

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val conversations: StateFlow<List<ChatConversation>> = combine(
        repository.getUsers(),
        searchQuery.debounce(300).distinctUntilChanged()
    ) { users, query ->
        val cleanQuery = query.trim()

        // Lọc danh sách user theo số điện thoại (nếu ô tìm kiếm có nội dung)
        val filteredUsers = if (cleanQuery.isBlank()) {
            users
        } else {
            users.filter { user ->
                user.phone.contains(cleanQuery)
            }
        }

        // Chuyển đổi danh sách User sang ChatConversation để hiển thị
        filteredUsers.map { user ->
            ChatConversation(
                otherUser = user,
                lastMessage = "Chạm để bắt đầu trò chuyện",
                lastTimestamp = 0L,
                lastSenderId = "",
                isRead = true
            )
        }
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