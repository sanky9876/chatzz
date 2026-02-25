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
            _messages.value = repository.getMessages(chatId)
        }
    }

    private fun observeNewMessages() {
        viewModelScope.launch {
            repository.observeMessages(chatId).collect { newMessage ->
                _messages.value = _messages.value + newMessage
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
