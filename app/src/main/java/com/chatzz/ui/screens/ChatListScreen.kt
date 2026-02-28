package com.chatzz.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chatzz.domain.models.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.chatzz.ui.viewmodels.ChatListViewModel
import com.chatzz.data.repositories.ChatRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(viewModel: ChatListViewModel, onChatClick: (String) -> Unit, onSignOut: () -> Unit) {
    val contacts by viewModel.contacts.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showAddFriendDialog by remember { mutableStateOf(false) }

    if (showAddFriendDialog) {
        AddFriendDialog(
            onDismiss = { showAddFriendDialog = false },
            onSendRequest = { email ->
                viewModel.sendFriendRequest(email)
                showAddFriendDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chatzz") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFriendDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Friend", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            errorMessage?.let { msg ->
                item {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (pendingRequests.isNotEmpty()) {
                item {
                    Text("Pending Requests", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                }
                items(pendingRequests) { pair ->
                    val request = pair.first
                    val user = pair.second
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.email ?: "Unknown", fontWeight = FontWeight.Bold)
                            Text("Wants to connect", color = Color.Gray, fontSize = 12.sp)
                        }
                        IconButton(onClick = { viewModel.acceptRequest(request.id) }) {
                            Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.Green)
                        }
                    }
                    Divider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            item {
                Text("Contacts", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            }
            if (contacts.isEmpty()) {
                item {
                    Text("No friends yet. Add someone!", modifier = Modifier.padding(16.dp), color = Color.Gray)
                }
            }
            items(contacts) { contact ->
                ContactItem(contact = contact, onClick = { 
                    if (contact.status == "accepted") {
                        viewModel.startChat(contact.user.id) { chatId ->
                            onChatClick(chatId)
                        }
                    }
                })
                Divider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun ContactItem(contact: ChatRepository.Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = contact.user.email ?: contact.user.id.take(8), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                text = if (contact.status == "accepted") "Friend" else "Request Sent", 
                color = if (contact.status == "accepted") Color.Gray else Color(0xFFFFA500), 
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AddFriendDialog(onDismiss: () -> Unit, onSendRequest: (String) -> Unit) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Friend") },
        text = {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Friend's Email") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onSendRequest(email) }) {
                Text("Send Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
