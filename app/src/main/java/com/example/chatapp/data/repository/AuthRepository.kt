package com.example.chatapp.data.repository

import com.example.chatapp.data.model.ChatConversation
import com.example.chatapp.data.model.ChatStatus
import com.example.chatapp.data.model.Message
import com.example.chatapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.os.Handler
import android.os.Looper
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://chatapp-7f0b4-default-rtdb.asia-southeast1.firebasedatabase.app"
    ),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun register(
        name: String,
        email: String,
        pass: String,
        phone: String
    ): Result<Boolean> {
        return try {
            val authResult = auth
                .createUserWithEmailAndPassword(email, pass)
                .await()

            val uid = authResult.user?.uid
                ?: throw Exception("Không lấy được UID người dùng")

            val user = User(
                uid = uid,
                name = name.trim(),
                email = email.trim(),
                status = "ONLINE",
                phone = phone.trim(),
                nameLowercase = name.trim().lowercase()
            )

            db.reference
                .child("users")
                .child(uid)
                .setValue(user)
                .await()
            goOnline()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        pass: String
    ): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            goOnline()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            auth.signOut()
            return
        }
        val statusRef = db.reference.child("users").child(uid).child("status")
        statusRef.onDisconnect().cancel()

        // Ghi OFFLINE trước rồi mới signOut; tối đa chờ 2 giây để mất mạng vẫn đăng xuất được
        var done = false
        val finish = {
            if (!done) {
                done = true
                auth.signOut()
            }
        }
        statusRef.setValue("OFFLINE").addOnCompleteListener { finish() }
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 2000)
    }

    fun getUsers(): Flow<List<User>> = callbackFlow {
        val usersRef = db.reference.child("users")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && user.uid != currentUserId) {
                        userList.add(user)
                    }
                }
                trySend(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        usersRef.addValueEventListener(listener)
        awaitClose { usersRef.removeEventListener(listener) }
    }

    fun getChatRoomId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }

    // Gửi tin nhắn: Tự động khởi tạo trạng thái PENDING nếu chưa từng chat, chặn nếu đã bị DECLINED
    suspend fun sendMediaMessage(
        receiverId: String,
        text: String = "",
        imageUrl: String? = null
    ): Result<Boolean> {
        val senderId = currentUserId ?: return Result.failure(Exception("Chưa đăng nhập"))
        val chatRoomId = getChatRoomId(senderId, receiverId)
        val statusRef = db.reference.child("chat_status").child(chatRoomId)

        return try {
            val snapshot = statusRef.get().await()
            val chatStatus = snapshot.getValue(ChatStatus::class.java)

            if (chatStatus == null) {
                val newStatus = ChatStatus(
                    chatRoomId = chatRoomId,
                    initiatorId = senderId,
                    receiverId = receiverId,
                    status = "PENDING"
                )
                statusRef.setValue(newStatus).await()
            } else if (chatStatus.status == "DECLINED") {
                return Result.failure(Exception("Cuộc trò chuyện đã bị từ chối"))
            }

            val messageRef = db.reference.child("chats").child(chatRoomId).push()
            val messageId = messageRef.key ?: ""

            val message = Message(
                messageId = messageId,
                senderId = senderId,
                receiverId = receiverId,
                text = text,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis()
            )

            messageRef.setValue(message).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessages(receiverId: String): Flow<List<Message>> = callbackFlow {
        val senderId = currentUserId ?: run {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val chatRoomId = getChatRoomId(senderId, receiverId)
        val messagesRef = db.reference.child("chats").child(chatRoomId).orderByChild("timestamp")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messageList = mutableListOf<Message>()
                for (msgSnapshot in snapshot.children) {
                    val message = msgSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        messageList.add(message)
                    }
                }
                trySend(messageList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        messagesRef.addValueEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    // Xóa trò chuyện
    suspend fun deleteConversation(receiverId: String): Result<Boolean> {
        val senderId = currentUserId ?: return Result.failure(Exception("Chưa đăng nhập"))
        val chatRoomId = getChatRoomId(senderId, receiverId)

        return try {
            db.reference.child("chats").child(chatRoomId).removeValue().await()
            db.reference.child("chat_status").child(chatRoomId).removeValue().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markMessagesAsRead(receiverId: String) {
        val senderId = currentUserId ?: return
        val chatRoomId = getChatRoomId(senderId, receiverId)
        val ref = db.reference.child("chats").child(chatRoomId)

        val snapshot = ref.get().await()
        for (msgSnapshot in snapshot.children) {
            val msg = msgSnapshot.getValue(Message::class.java)
            if (msg != null && msg.receiverId == senderId && !msg.isRead) {
                msgSnapshot.ref.child("isRead").setValue(true)
            }
        }
    }

    fun getRecentConversations(): Flow<List<ChatConversation>> = callbackFlow {
        val myUid = currentUserId ?: run {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val chatsRef = db.reference.child("chats")
        val usersRef = db.reference.child("users")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversationMap = mutableMapOf<String, ChatConversation>()
                var matchedRooms = 0
                for (roomSnapshot in snapshot.children) {
                    val roomId = roomSnapshot.key ?: continue
                    if (roomId.contains(myUid)) {
                        val messages = mutableListOf<Message>()
                        for (msgSnapshot in roomSnapshot.children) {
                            val msg = msgSnapshot.getValue(Message::class.java)
                            if (msg != null) messages.add(msg)
                        }

                        if (messages.isNotEmpty()) {
                            val lastMsg = messages.maxByOrNull { it.timestamp }!!
                            val otherUserId =
                                if (lastMsg.senderId == myUid) lastMsg.receiverId else lastMsg.senderId
                            matchedRooms++
                            usersRef.child(otherUserId).get().addOnSuccessListener { userSnapshot ->
                                val otherUser = userSnapshot.getValue(User::class.java)
                                if (otherUser != null) {
                                    val isRead =
                                        if (lastMsg.senderId != myUid) lastMsg.isRead else true
                                    conversationMap[otherUserId] = ChatConversation(
                                        otherUser = otherUser,
                                        lastMessage = lastMsg.text,
                                        lastTimestamp = lastMsg.timestamp,
                                        lastSenderId = lastMsg.senderId,
                                        isRead = isRead
                                    )
                                    val sortedList =
                                        conversationMap.values.sortedByDescending { it.lastTimestamp }
                                    trySend(sortedList)
                                }
                            }
                        }
                    }
                }
                if (matchedRooms == 0) {
                    trySend(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        chatsRef.addValueEventListener(listener)
        awaitClose { chatsRef.removeEventListener(listener) }
    }

    fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        val uid = currentUserId ?: run {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val userRef = db.reference.child("users").child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                trySend(user)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        userRef.addValueEventListener(listener)
        awaitClose { userRef.removeEventListener(listener) }
    }

    suspend fun updateUserProfile(newName: String, newPhone: String): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            val updates = mapOf(
                "name" to newName,
                "phone" to newPhone
            )
            db.reference.child("users").child(uid).updateChildren(updates).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lắng nghe trạng thái Chấp nhận/Từ chối của cuộc trò chuyện
    fun getChatStatus(receiverId: String): Flow<ChatStatus?> = callbackFlow {
        val myUid = currentUserId ?: run {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val chatRoomId = getChatRoomId(myUid, receiverId)
        val statusRef = db.reference.child("chat_status").child(chatRoomId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(ChatStatus::class.java)
                trySend(status)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        statusRef.addValueEventListener(listener)
        awaitClose { statusRef.removeEventListener(listener) }
    }

    // Cập nhật trạng thái cuộc trò chuyện (ACCEPT hoặc DECLINE)
    suspend fun updateChatStatus(receiverId: String, newStatus: String): Result<Boolean> {
        val myUid = currentUserId ?: return Result.failure(Exception("Chưa đăng nhập"))
        val chatRoomId = getChatRoomId(myUid, receiverId)
        return try {
            db.reference.child("chat_status").child(chatRoomId).child("status").setValue(newStatus)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hàm chuyển đổi Uri hình ảnh thành chuỗi Base64 (đã nén nhỏ kích thước)
    fun convertUriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            // Nén kích thước ảnh để giảm dung lượng lưu trữ trên Realtime Database
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 600, 600, true)

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()

            "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Xóa tin nhắn (Cập nhật text thành "Tin nhắn đã được xóa" và ẩn imageUrl)
    suspend fun deleteMessage(receiverId: String, messageId: String): Result<Boolean> {
        val senderId = currentUserId ?: return Result.failure(Exception("Chưa đăng nhập"))
        val chatRoomId = getChatRoomId(senderId, receiverId)
        val msgRef = db.reference.child("chats").child(chatRoomId).child(messageId)

        return try {
            val updates = mapOf(
                "text" to "Tin nhắn đã được xóa",
                "imageUrl" to null
            )
            msgRef.updateChildren(updates).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Chỉnh sửa nội dung tin nhắn
    suspend fun editMessage(
        receiverId: String,
        messageId: String,
        newText: String
    ): Result<Boolean> {
        val senderId = currentUserId ?: return Result.failure(Exception("Chưa đăng nhập"))
        val chatRoomId = getChatRoomId(senderId, receiverId)
        val msgRef = db.reference.child("chats").child(chatRoomId).child(messageId)

        return try {
            msgRef.child("text").setValue(newText).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun goOnline() {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.reference.child("users").child(uid).child("status")
        ref.onDisconnect().setValue("OFFLINE")   // mất kết nối -> Firebase tự đặt OFFLINE
        ref.setValue("ONLINE")
    }

    fun goOffline() {
        val uid = auth.currentUser?.uid ?: return
        db.reference.child("users").child(uid).child("status").setValue("OFFLINE")
    }

    fun getUserStatus(userId: String): Flow<String> = callbackFlow {
        val ref = db.reference.child("users").child(userId).child("status")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(String::class.java) ?: "OFFLINE")
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
        //đổi mật khẩu
        fun changePassword(
            oldPass: String,
            newPass: String,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
        ) {
            val user = auth.currentUser
            if (user != null && user.email != null) {
                // 1. Xác thực lại với mật khẩu cũ
                val credential =
                    com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, oldPass)
                user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                    if (reAuthTask.isSuccessful) {
                        // 2. Nếu đúng MK cũ -> Tiến hành cập nhật MK mới
                        user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                onSuccess()
                            } else {
                                onError(
                                    updateTask.exception?.localizedMessage
                                        ?: "Cập nhật mật khẩu thất bại"
                                )
                            }
                        }
                    } else {
                        onError("Mật khẩu hiện tại không chính xác")
                    }
                }
            } else {
                onError("Không tìm thấy thông tin đăng nhập")
            }
        }
    }
