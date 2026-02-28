package com.chatzz.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatzz.data.repositories.ChatRepository
import com.chatzz.domain.models.Chat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.chatzz.domain.models.FriendRequest
import com.chatzz.domain.models.User

class ChatListViewModel(
    private val userId: String,
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    private val _contacts = MutableStateFlow<List<ChatRepository.Contact>>(emptyList())
    val contacts: StateFlow<List<ChatRepository.Contact>> = _contacts

    private val _pendingRequests = MutableStateFlow<List<Pair<FriendRequest, User>>>(emptyList())
    val pendingRequests: StateFlow<List<Pair<FriendRequest, User>>> = _pendingRequests

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _chats.value = repository.getChatsForUser(userId)
                _contacts.value = repository.getContacts(userId)
                _pendingRequests.value = repository.getPendingRequests(userId)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendFriendRequest(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.sendFriendRequest(userId, email)
                _errorMessage.value = "Request Sent!"
                loadData()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            try {
                repository.acceptRequest(requestId)
                loadData()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun startChat(contactId: String, onChatCreated: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val chatId = repository.getOrCreateChat(userId, contactId)
                loadData()
                onChatCreated(chatId)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
