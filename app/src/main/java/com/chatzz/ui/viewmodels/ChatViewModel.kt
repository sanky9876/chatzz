package com.chatzz.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatzz.data.repositories.ChatRepository
import com.chatzz.domain.models.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatId: String,
    private val currentUserId: String,
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    init {
        loadMessages()
        observeNewMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            val msgs = repository.getMessages(chatId)
            _messages.value = msgs
            if (msgs.any { it.sender_id != currentUserId && it.read_at == null }) {
                repository.markMessagesAsRead(chatId, currentUserId)
            }
        }
    }

    private fun observeNewMessages() {
        viewModelScope.launch {
            repository.observeMessages(chatId).collect { newMessage ->
                val currentList = _messages.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { it.id == newMessage.id }
                if (existingIndex >= 0) {
                    currentList[existingIndex] = newMessage
                } else {
                    currentList.add(newMessage)
                }
                _messages.value = currentList
                
                if (newMessage.sender_id != currentUserId && newMessage.read_at == null) {
                    repository.markMessagesAsRead(chatId, currentUserId)
                }
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            repository.sendMessage(chatId, currentUserId, content)
        }
    }

    fun deleteChat() {
        viewModelScope.launch {
            repository.deleteChat(chatId)
        }
    }
}
