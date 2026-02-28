package com.chatzz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatzz.domain.models.Message
import com.chatzz.ui.viewmodels.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(viewModel: ChatViewModel, currentUserId: String, onNavigateBack: () -> Unit) {
    val messages by viewModel.messages.collectAsState()
    var textState by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.deleteChat()
                        onNavigateBack()
                    }) {
                        Text("Delete", color = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message") }
                )
                IconButton(onClick = {
                    if (textState.isNotBlank()) {
                        viewModel.sendMessage(textState)
                        textState = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFECE5DD)), // WhatsApp background color
            contentPadding = PaddingValues(8.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                MessageBubble(message = message, isCurrentUser = message.sender_id == currentUserId)
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val color = if (isCurrentUser) Color(0xFFDCF8C6) else Color.White
    val shape = if (isCurrentUser) {
        RoundedCornerShape(8.dp, 8.dp, 0.dp, 8.dp)
    } else {
        RoundedCornerShape(0.dp, 8.dp, 8.dp, 8.dp)
    }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = alignment) {
        Surface(
            color = color,
            shape = shape,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = message.content, fontSize = 16.sp)
                
                val timeString = try {
                    message.created_at?.let { iso ->
                        val instant = java.time.Instant.parse(iso)
                        val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a").withZone(java.time.ZoneId.systemDefault())
                        formatter.format(instant)
                    } ?: ""
                } catch (e: Exception) { "" }

                Row(modifier = Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = timeString, fontSize = 10.sp, color = Color.Gray)
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        if (message.read_at != null) {
                            Row {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34B7F1), modifier = Modifier.size(16.dp))
                                Icon(Icons.Default.Check, contentDescription = "Read", tint = Color(0xFF34B7F1), modifier = Modifier.size(16.dp).offset(x = (-8).dp))
                            }
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Sent", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
