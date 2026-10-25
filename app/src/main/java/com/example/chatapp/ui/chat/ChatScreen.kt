package com.example.chatapp.ui.chat

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.chatapp.data.model.Message
import com.example.chatapp.ui.home.ZaloPrimaryColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.SolidColor
import com.example.chatapp.ui.theme.AppThemeOption
import com.example.chatapp.ui.theme.LocalAppColors
import com.example.chatapp.ui.theme.LocalThemeController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    receiverId: String,
    receiverName: String,
    onBackClick: () -> Unit,
    onNavigateToThemeSettings: () -> Unit = {},
    viewModel: ChatViewModel = viewModel()
) {
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val chatStatus by viewModel.chatStatus.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val receiverStatus by viewModel.receiverStatus.collectAsState()
    val currentUserId = viewModel.currentUserId
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // State quản lý việc ẩn/hiện Menu 3 chấm ở TopAppBar
    var showTopMenu by remember { mutableStateOf(false) }

    // State màu nền khung Chat (Mặc định là màu #E2E7ED)
    // Màu lấy từ theme chung của cả app
    val c = LocalAppColors.current
    val themeController = LocalThemeController.current
    val chatBackgroundColor = c.chatBackground
    val isDarkBg = c.isDark
    val barColor = c.surface
    val inputBg = c.inputField
    val mainText = c.textPrimary
    val subText = c.textSecondary

    var showBackgroundDialog by remember { mutableStateOf(false) }
    var showUnfriendDialog by remember { mutableStateOf(false) }
    // State quản lý tin nhắn đang chọn chỉnh sửa
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var editTextFieldValue by remember { mutableStateOf("") }

    // Trình chọn ảnh từ thiết bị
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            viewModel.sendImageMessage(context, receiverId, selectedUri)
        }
    }

    // Lời mời nhắn tin chờ người dùng hiện tại xử lý
    val isPendingForMe = chatStatus?.status == "PENDING" && chatStatus?.receiverId == currentUserId
    // Lời mời nhắn tin đã bị đối phương từ chối
    val isDeclinedByOther = chatStatus?.status == "DECLINED" && chatStatus?.initiatorId == currentUserId

    // Tải danh sách tin nhắn khi vào màn hình
    LaunchedEffect(receiverId) {
        viewModel.loadMessages(receiverId)
    }

    // Tự động cuộn xuống tin nhắn mới nhất
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZaloPrimaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = receiverName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = receiverName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (receiverStatus == "ONLINE") "Đang hoạt động" else "Không hoạt động",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Phone, contentDescription = "Call") }
                    IconButton(onClick = {}) { Icon(Icons.Default.Videocam, contentDescription = "Video Call") }
                    // --- NÚT 3 CHẤM TOP APP BAR ---
                    Box {
                        IconButton(onClick = { showTopMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }

                        DropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false }
                        ) {
                            // 1. Chỉnh sửa màu nền
                            DropdownMenuItem(
                                text = { Text("Chỉnh sửa màu nền") },
                                leadingIcon = {
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = Color.Gray)
                                },
                                onClick = {
                                    showTopMenu = false
                                    showBackgroundDialog = true
                                }
                            )

                            // 2. Xóa bạn (Đưa về trạng thái chưa kết bạn/xóa quan hệ)
                            DropdownMenuItem(
                                text = { Text("Xóa bạn", color = Color.Red) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color.Red
                                    )
                                },
                                onClick = {
                                    showTopMenu = false
                                    showUnfriendDialog = true
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = barColor,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                when {
                    // 1. Trường hợp người dùng nhận được yêu cầu nhắn tin: Hiện banner Chấp nhận / Từ chối
                    isPendingForMe -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Người này muốn nhắn tin với bạn",
                                fontSize = 14.sp,
                                color = subText
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.declineChat(receiverId) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                ) {
                                    Text("Từ chối")
                                }
                                Button(
                                    onClick = { viewModel.acceptChat(receiverId) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = ZaloPrimaryColor)
                                ) {
                                    Text("Chấp nhận")
                                }
                            }
                        }
                    }

                    // 2. Trường hợp bạn đã bị từ chối: Hiển thị thông báo khóa
                    isDeclinedByOther -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF0F0))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Không thể gửi tin nhắn do người dùng đã từ chối nhận tin nhắn từ bạn.",
                                color = Color.Red,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 3. Khung nhập tin nhắn phong cách Zalo chuẩn
                    else -> {
                        Column {
                            if (isUploading) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = ZaloPrimaryColor
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Image",
                                        tint = subText
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 40.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(inputBg)
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    BasicTextField(
                                        value = messageText,
                                        onValueChange = { messageText = it },
                                        textStyle = TextStyle(fontSize = 15.sp, color = mainText),
                                        cursorBrush = SolidColor(ZaloPrimaryColor),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (messageText.isEmpty()) {
                                                Text("Tin nhắn", color = subText, fontSize = 15.sp)
                                            }
                                            innerTextField()
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Nút gửi
                                IconButton(
                                    onClick = {
                                        if (messageText.isNotBlank()) {
                                            viewModel.sendMessage(receiverId, messageText)
                                            messageText = ""
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = if (messageText.isNotBlank()) ZaloPrimaryColor else subText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(chatBackgroundColor)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(messages) { message ->
                    val isMe = message.senderId == viewModel.currentUserId
                    ChatMessageItem(
                        message = message,
                        isMe = isMe,
                        darkMode = isDarkBg,
                        onEditClick = {
                            editingMessage = message
                            editTextFieldValue = message.text
                        },
                        onDeleteClick = {
                            viewModel.deleteMessage(receiverId, message.messageId)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Dialog Chỉnh sửa tin nhắn
        if (editingMessage != null) {
            AlertDialog(
                onDismissRequest = { editingMessage = null },
                title = { Text("Chỉnh sửa tin nhắn") },
                text = {
                    OutlinedTextField(
                        value = editTextFieldValue,
                        onValueChange = { editTextFieldValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            editingMessage?.let { msg ->
                                viewModel.editMessage(receiverId, msg.messageId, editTextFieldValue)
                            }
                            editingMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZaloPrimaryColor)
                    ) {
                        Text("Lưu")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingMessage = null }) {
                        Text("Hủy")
                    }
                }
            )
        }

        // Chọn theme: đổi màu cả app
        if (showBackgroundDialog) {
            AlertDialog(
                onDismissRequest = { showBackgroundDialog = false },
                title = { Text("Chỉnh sửa màu nền") },
                text = {
                    Column {
                        AppThemeOption.entries.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { themeController.onChange(option) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(option.colors.background)
                                        .border(1.dp, c.divider, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(option.colors.accent)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = option.label,
                                    color = c.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                RadioButton(
                                    selected = option == themeController.current,
                                    onClick = { themeController.onChange(option) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBackgroundDialog = false }) { Text("Xong") }
                }
            )
        }

        // Xác nhận xóa bạn
        if (showUnfriendDialog) {
            AlertDialog(
                onDismissRequest = { showUnfriendDialog = false },
                title = { Text("Xóa bạn") },
                text = {
                    Text("Xóa toàn bộ tin nhắn với $receiverName? Hành động này không thể hoàn tác và tin nhắn cũng sẽ mất ở phía $receiverName.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        showUnfriendDialog = false
                        viewModel.unfriend(receiverId) { success ->
                            if (success) {
                                onBackClick()
                            } else {
                                Toast.makeText(context, "Xóa thất bại, thử lại nhé", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Text("Xóa", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUnfriendDialog = false }) { Text("Hủy") }
                }
            )
        }
    }
}

@Composable

fun ChatMessageItem(
    message: Message,
    isMe: Boolean,
    darkMode: Boolean = false,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val isDeleted = message.text == "Tin nhắn đã được xóa"

    val formattedTime = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }
    val imageBitmap = remember(message.imageUrl) {
        if (!message.imageUrl.isNullOrEmpty()) {
            try {
                val pureBase64 = if (message.imageUrl.contains(",")) {
                    message.imageUrl.substringAfter(",")
                } else {
                    message.imageUrl
                }
                val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                bitmap?.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else null
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
        ) {
            Box {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 2.dp,
                        bottomEnd = if (isMe) 2.dp else 16.dp
                    ),
                    color = when {
                        isDeleted -> if (darkMode) Color(0xFF242424) else Color(0xFFF0F0F0)
                        isMe -> if (darkMode) Color(0xFF2B4C7E) else Color(0xFFE3F2FD)
                        else -> if (darkMode) Color(0xFF2A2A2A) else Color.White
                    },
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    if (isMe && !isDeleted) {
                                        showMenu = true
                                    }
                                }
                            )
                        }
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        if (!isDeleted) {
                            if (imageBitmap != null) {
                                Image(
                                    bitmap = imageBitmap,
                                    contentDescription = "Hình ảnh nhắn tin",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                if (message.text.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            } else if (!message.imageUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = message.imageUrl,
                                    contentDescription = "Hình ảnh nhắn tin",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                if (message.text.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }

                        if (message.text.isNotBlank()) {
                            Text(
                                text = message.text,
                                fontSize = 15.sp,
                                color = when {
                                    isDeleted -> if (darkMode) Color(0xFFA0A0A0) else Color.Gray
                                    darkMode -> Color(0xFFF2F2F2)
                                    else -> Color.Black
                                },
                                fontStyle = if (isDeleted) FontStyle.Italic else FontStyle.Normal,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Chỉnh sửa") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Xóa tin nhắn", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}