package com.chatzz.data.repositories

import com.chatzz.data.SupabaseInstance
import com.chatzz.domain.models.Chat
import com.chatzz.domain.models.Message
import com.chatzz.domain.models.User
import com.chatzz.domain.models.FriendRequest
import com.chatzz.domain.models.ChatMember
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.decodeFromJsonElement

class ChatRepository {
    private val client = SupabaseInstance.client

    suspend fun getChatsForUser(userId: String): List<Chat> {
        return client.postgrest["chats"].select().decodeList<Chat>()
    }

    suspend fun getMessages(chatId: String): List<Message> {
        return client.postgrest["messages"]
            .select {
                filter {
                    eq("chat_id", chatId)
                }
                order("created_at", Order.ASCENDING)
            }.decodeList<Message>()
    }

    suspend fun sendMessage(chatId: String, senderId: String, content: String) {
        val message = Message(chat_id = chatId, sender_id = senderId, content = content)
        client.postgrest["messages"].insert(message)
    }

    fun observeMessages(chatId: String): Flow<Message> {
        val channel = client.realtime.channel("public:messages")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
        }.mapNotNull { action -> 
            when (action) {
                is PostgresAction.Insert -> kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromJsonElement<Message>(action.record)
                is PostgresAction.Update -> kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromJsonElement<Message>(action.record)
                else -> null
            }
        }
        .filter { it.chat_id == chatId }
        .onStart {
            channel.subscribe()
        }
    }

    suspend fun markMessagesAsRead(chatId: String, currentUserId: String) {
        val unreadMessages = getMessages(chatId).filter { it.sender_id != currentUserId && it.read_at == null }
        if (unreadMessages.isEmpty()) return

        val unreadIds = unreadMessages.mapNotNull { it.id }
        
        @kotlinx.serialization.Serializable
        data class ReadUpdate(val read_at: String)

        client.postgrest["messages"].update(ReadUpdate(read_at = java.time.Instant.now().toString())) {
            filter { isIn("id", unreadIds) }
        }
    }

    suspend fun deleteChat(chatId: String) {
        client.postgrest["chats"].delete {
            filter { eq("id", chatId) }
        }
    }

    suspend fun sendFriendRequest(senderId: String, receiverEmail: String) {
        val targetUsers = client.postgrest["users"].select {
            filter { eq("email", receiverEmail) }
        }.decodeList<User>()

        if (targetUsers.isEmpty()) throw Exception("User not found with this email.")
        val targetUser = targetUsers.first()

        if (targetUser.id == senderId) throw Exception("Cannot add yourself.")

        val existing = client.postgrest["friend_requests"].select {
            filter {
                or {
                    eq("sender_id", senderId)
                    eq("receiver_id", senderId)
                }
            }
        }.decodeList<FriendRequest>()

        val alreadyExists = existing.any { 
            (it.sender_id == targetUser.id && it.receiver_id == senderId) || 
            (it.sender_id == senderId && it.receiver_id == targetUser.id) 
        }

        if (alreadyExists) {
            throw Exception("Request or friendship already exists.")
        }

        @kotlinx.serialization.Serializable
        data class RequestInsert(val sender_id: String, val receiver_id: String, val status: String)

        client.postgrest["friend_requests"].insert(
            RequestInsert(sender_id = senderId, receiver_id = targetUser.id, status = "pending")
        )
    }

    suspend fun acceptRequest(requestId: String) {
        @kotlinx.serialization.Serializable
        data class RequestUpdate(val status: String)

        client.postgrest["friend_requests"].update(RequestUpdate(status = "accepted")) {
            filter { eq("id", requestId) }
        }
    }

    suspend fun rejectRequest(requestId: String) {
        @kotlinx.serialization.Serializable
        data class RequestUpdate(val status: String)

        client.postgrest["friend_requests"].update(RequestUpdate(status = "rejected")) {
            filter { eq("id", requestId) }
        }
    }

    suspend fun getPendingRequests(currentUserId: String): List<Pair<FriendRequest, User>> {
        val requests = client.postgrest["friend_requests"].select {
            filter {
                eq("receiver_id", currentUserId)
                eq("status", "pending")
            }
        }.decodeList<FriendRequest>()

        if (requests.isEmpty()) return emptyList()

        val senderIds = requests.map { it.sender_id }.distinct()
        val users = client.postgrest["users"].select {
            filter {
                isIn("id", senderIds)
            }
        }.decodeList<User>()

        return requests.mapNotNull { req -> 
            val user = users.find { it.id == req.sender_id }
            if (user != null) req to user else null
        }
    }

    data class Contact(val user: User, val status: String)

    suspend fun getContacts(currentUserId: String): List<Contact> {
        val allRequests = client.postgrest["friend_requests"].select {
            filter {
                or {
                    eq("sender_id", currentUserId)
                    eq("receiver_id", currentUserId)
                }
            }
        }.decodeList<FriendRequest>()

        val userIdsToFetch = mutableSetOf<String>()
        val contactMap = mutableMapOf<String, String>()

        for (req in allRequests) {
            if (req.status == "accepted") {
                val friendId = if (req.sender_id == currentUserId) req.receiver_id else req.sender_id
                userIdsToFetch.add(friendId)
                contactMap[friendId] = "accepted"
            } else if (req.status == "pending" && req.sender_id == currentUserId) {
                userIdsToFetch.add(req.receiver_id)
                contactMap[req.receiver_id] = "pending_outgoing"
            }
        }

        if (userIdsToFetch.isEmpty()) return emptyList()

        val users = client.postgrest["users"].select {
            filter {
                isIn("id", userIdsToFetch.toList())
            }
        }.decodeList<User>()

        return users.map { Contact(it, contactMap[it.id] ?: "unknown") }
    }

    suspend fun getOrCreateChat(currentUserId: String, targetUserId: String): String {
        val myChats = client.postgrest["chat_members"].select {
            filter { eq("user_id", currentUserId) }
        }.decodeList<ChatMember>()

        if (myChats.isNotEmpty()) {
            val myChatIds = myChats.map { it.chat_id }
            val sharedChats = client.postgrest["chat_members"].select {
                filter {
                    eq("user_id", targetUserId)
                    isIn("chat_id", myChatIds)
                }
            }.decodeList<ChatMember>()

            if (sharedChats.isNotEmpty()) {
                return sharedChats.first().chat_id
            }
        }

        val newChatId = java.util.UUID.randomUUID().toString()
        client.postgrest["chats"].insert(Chat(id = newChatId))

        client.postgrest["chat_members"].insert(
            listOf(
                ChatMember(chat_id = newChatId, user_id = currentUserId),
                ChatMember(chat_id = newChatId, user_id = targetUserId)
            )
        )

        return newChatId
    }

    suspend fun updateLastSeen(userId: String) {
        @kotlinx.serialization.Serializable
        data class LastSeenUpdate(val last_seen: String)

        try {
            client.postgrest["users"].update(LastSeenUpdate(java.time.Instant.now().toString())) {
                filter { eq("id", userId) }
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}
