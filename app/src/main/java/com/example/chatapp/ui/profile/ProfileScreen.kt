package com.example.chatapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatapp.data.model.User
import com.example.chatapp.ui.home.ZaloPrimaryColor
import com.example.chatapp.ui.theme.LocalAppColors

import android.widget.Toast
import androidx.compose.material.icons.filled.Lock // [THÊM MỚI: Icon khóa cho nút Đổi mật khẩu]
import androidx.compose.runtime.* // [THÊM MỚI: Thêm mutableStateOf, remember]
import androidx.compose.ui.platform.LocalContext // [THÊM MỚI: Lấy Context cho Toast]
import androidx.compose.ui.text.input.PasswordVisualTransformation // [THÊM MỚI: Ẩn mật khẩu bằng dấu chấm]
import com.example.chatapp.ui.auth.AuthViewModel // [THÊM MỚI: Nhận ViewModel để gọi hàm đổi mật khẩu]

@Composable
fun ProfileScreen(
    user: User?,
    isEditing: Boolean,
    editName: String,
    editPhone: String,
    onStartEdit: () -> Unit,
    onEditNameChange: (String) -> Unit,
    onEditPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel() // <-- THÊM DÒNG NÀY VÀO
) {
    val c = LocalAppColors.current
    // ================= [THÊM MỚI: Quản lý trạng thái Dialog đổi mật khẩu] =================
    var showPasswordDialog by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    val context = LocalContext.current
    // ===================================================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card chứa thông tin cá nhân
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = c.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Đại diện (Lấy chữ cái đầu)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(c.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user?.name?.take(1)?.uppercase() ?: "U",
                        color = c.onAccent,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditing) {
                    // CHẾ ĐỘ SỬA: Cho phép nhập lại Họ tên & Số điện thoại
                    OutlinedTextField(
                        value = editName,
                        onValueChange = onEditNameChange,
                        label = { Text("Họ và tên") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = onEditPhoneChange,
                        label = { Text("Số điện thoại") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.Close, contentDescription = "Hủy", tint = Color.Red)
                        }
                        IconButton(onClick = onSave) {
                            Icon(Icons.Default.Check, contentDescription = "Lưu", tint = Color(0xFF4CAF50))
                        }
                    }
                } else {
                    // CHẾ ĐỘ XEM: Hiển thị tên, email, SĐT + Nút Edit
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = user?.name ?: "Người dùng",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.textPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onStartEdit,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Chỉnh sửa",
                                tint = c.accent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = user?.email ?: "",
                        fontSize = 14.sp,
                        color = c.textSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Hiển thị Số điện thoại
                    Text(
                        text = if (!user?.phone.isNull_or_blank()) "SĐT: ${user?.phone}" else "Chưa cập nhật SĐT",
                        fontSize = 14.sp,
                        color = c.textTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // ================= [THÊM MỚI: NÚT ĐỔI MẬT KHẨU] =================
        OutlinedButton(
            onClick = { showPasswordDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = ZaloPrimaryColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đổi mật khẩu", color = ZaloPrimaryColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))
        // ================================================================

        // Nút Đăng xuất ở cuối màn hình
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4F)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng xuất", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }

    // ================= [THÊM MỚI: HỘP THOẠI DIALOG ĐỔI MẬT KHẨU] =================
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                oldPassword = ""
                newPassword = ""
            },
            title = { Text("Đổi mật khẩu tài khoản", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Mật khẩu hiện tại") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Mật khẩu mới (tối thiểu 6 ký tự)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ mật khẩu!", Toast.LENGTH_SHORT).show()
                        } else if (newPassword.length < 6) {
                            Toast.makeText(context, "Mật khẩu mới phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show()
                        } else {
                            // Gọi hàm trong AuthViewModel
                            authViewModel.changePassword(
                                oldPass = oldPassword,
                                newPass = newPassword,
                                onSuccess = {
                                    Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                                    showPasswordDialog = false
                                    oldPassword = ""
                                    newPassword = ""
                                },
                                onError = { errorMsg ->
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                ) {
                    Text("Cập nhật")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        oldPassword = ""
                        newPassword = ""
                    }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
    // ============================================================================
}

// Extension nhỏ hỗ trợ check null/blank
private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()