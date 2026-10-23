package com.example.chatapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatapp.data.model.ChatConversation
import com.example.chatapp.ui.profile.ProfileScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val ZaloPrimaryColor = Color(0xFF0068FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onUserClick: (userId: String, userName: String) -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val editName by viewModel.editName.collectAsState()
    val editPhone by viewModel.editPhone.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZaloPrimaryColor,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = {
                    if (selectedTab == 2) {
                        Text(
                            text = "Tài khoản cá nhân",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = {
                                Text(
                                    "Tìm kiếm theo số điện thoại",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        )
                    }
                },
                actions = {
                    if (selectedTab != 2) {
                        IconButton(onClick = {
                            viewModel.logout()
                            onLogout()
                        }) {
                            Icon(Icons.Default.Logout, contentDescription = "Đăng xuất")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Tin nhắn") },
                    label = { Text("Tin nhắn") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZaloPrimaryColor,
                        selectedTextColor = ZaloPrimaryColor,
                        indicatorColor = Color(0xFFE8F1FF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Phone, contentDescription = "Cuộc gọi") },
                    label = { Text("Danh bạ") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZaloPrimaryColor,
                        selectedTextColor = ZaloPrimaryColor,
                        indicatorColor = Color(0xFFE8F1FF)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Cá nhân") },
                    label = { Text("Cá nhân") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZaloPrimaryColor,
                        selectedTextColor = ZaloPrimaryColor,
                        indicatorColor = Color(0xFFE8F1FF)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF4F5F7))
        ) {
            when (selectedTab) {

                0 -> {
                    // TAB 1: DANH SÁCH TOÀN BỘ NGƯỜI DÙNG TÊN HỆ THỐNG
                    if (conversations.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isEmpty()) "Chưa có người dùng nào" else "Không tìm thấy kết quả",
                                color = Color.Gray,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(conversations) { item ->
                                ZaloConversationItem(
                                    conversation = item,
                                    onClick = { onUserClick(item.otherUser.uid, item.otherUser.name) }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 76.dp),
                                    thickness = 0.5.dp,
                                    color = Color(0xFFE5E5E5)
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 2: DANH BẠ
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Danh bạ chưa có dữ liệu", color = Color.Gray, fontSize = 15.sp)
                    }
                }

                2 -> {
                    // TAB 3: CÁ NHÂN
                    ProfileScreen(
                        user = currentUser,
                        isEditing = isEditing,
                        editName = editName,
                        editPhone = editPhone,
                        onStartEdit = { viewModel.startEditing() },
                        onEditNameChange = { viewModel.onEditNameChanged(it) },
                        onEditPhoneChange = { viewModel.onEditPhoneChanged(it) },
                        onSave = { viewModel.saveProfile() },
                        onCancel = { viewModel.cancelEditing() },
                        onLogout = {
                            viewModel.logout()
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ZaloConversationItem(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    val user = conversation.otherUser
    val isUnread = !conversation.isRead
    val formattedTime = remember(conversation.lastTimestamp) {
        if (conversation.lastTimestamp == 0L) ""
        else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(conversation.lastTimestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isUnread) Color(0xFFEDF5FF) else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(50.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(ZaloPrimaryColor.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (user.status == "ONLINE") {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.name,
                    fontSize = 16.sp,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formattedTime,
                    fontSize = 12.sp,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                    color = if (isUnread) ZaloPrimaryColor else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.lastMessage,
                    fontSize = 14.sp,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                    color = if (isUnread) Color.Black else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (isUnread) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ZaloPrimaryColor)
                    )
                }
            }
        }
    }
}