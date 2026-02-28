package com.chatzz.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatzz.data.repositories.ChatRepository
import com.chatzz.domain.models.Chat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val userId: String,
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadChats()
    }

    fun loadChats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _chats.value = repository.getChatsForUser(userId)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
