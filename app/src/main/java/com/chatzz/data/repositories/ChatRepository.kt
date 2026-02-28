package com.chatzz.data.repositories

import com.chatzz.data.SupabaseInstance
import com.chatzz.domain.models.Chat
import com.chatzz.domain.models.Message
import io.github.jan_tennert.supabase.postgrest.postgrest
import io.github.jan_tennert.supabase.postgrest.query.Order
import io.github.jan_tennert.supabase.realtime.Realtime
import io.github.jan_tennert.supabase.realtime.PostgresAction
import io.github.jan_tennert.supabase.realtime.PostgresJoinConfig
import io.github.jan_tennert.supabase.realtime.channel
import io.github.jan_tennert.supabase.realtime.postgresChangeFlow
import io.github.jan_tennert.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
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
        return channel.postgresChangeFlow<PostgresAction.Insert>(
            schema = "public",
            table = "messages"
        ).map { it.record.let { json -> client.postgrest.config.json.decodeFromJsonElement<Message>(json) } }
        .filter { it.chat_id == chatId }
    }

    suspend fun deleteChat(chatId: String) {
        client.postgrest["chats"].delete {
            filter { eq("id", chatId) }
        }
    }
}
