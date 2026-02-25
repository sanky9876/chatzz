package com.chatzz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chatzz.ui.screens.ChatDetailScreen
import com.chatzz.ui.screens.ChatListScreen
import com.chatzz.ui.screens.LoginScreen
import com.chatzz.ui.theme.ChatzzTheme
import com.chatzz.ui.viewmodels.AuthViewModel
import com.chatzz.ui.viewmodels.ChatViewModel
import com.chatzz.data.repositories.AuthRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authRepository = AuthRepository()
        
        setContent {
            ChatzzTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
                val authViewModel: AuthViewModel = viewModel()
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        currentScreen = Screen.ChatList
                    }
                }

                when (val screen = currentScreen) {
                    is Screen.Login -> LoginScreen(authViewModel)
                    is Screen.ChatList -> {
                        // In a real app, you'd fetch chats here. 
                        // For MVP, using a dummy list or empty list.
                        ChatListScreen(chats = emptyList()) { chatId ->
                            currentScreen = Screen.ChatDetail(chatId)
                        }
                    }
                    is Screen.ChatDetail -> {
                        val chatViewModel = ChatViewModel(
                            chatId = screen.chatId,
                            currentUserId = authRepository.getCurrentUserId() ?: ""
                        )
                        ChatDetailScreen(chatViewModel, authRepository.getCurrentUserId() ?: "")
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object Login : Screen()
    object ChatList : Screen()
    data class ChatDetail(val chatId: String) : Screen()
}
