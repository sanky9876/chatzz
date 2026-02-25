package com.chatzz.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String? = null,
    val avatar_url: String? = null,
    val created_at: String? = null
)

@Serializable
data class Chat(
    val id: String,
    val created_at: String? = null,
    val last_message_at: String? = null
)

@Serializable
data class ChatMember(
    val chat_id: String,
    val user_id: String
)

@Serializable
data class Message(
    val id: String? = null,
    val chat_id: String,
    val sender_id: String,
    val content: String,
    val created_at: String? = null,
    val read_at: String? = null
)
