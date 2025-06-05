package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tazy.meuapp.model.ChatMessage
import com.tazy.meuapp.viewmodel.ChatUiState
import com.tazy.meuapp.viewmodel.ChatIndividualViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaChatIndividual(
    navController: NavController,
    matchId: String,
    chatViewModel: ChatIndividualViewModel = hiltViewModel()
) {
    var messageText by remember { mutableStateOf("") }
    val chatState by chatViewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()

    // Carregar mensagens quando a tela é aberta
    LaunchedEffect(matchId) {
        chatViewModel.loadMessages(matchId)
    }

    // Auto-scroll para a última mensagem
    LaunchedEffect(chatState) {
        val currentChatState = chatState
        if (currentChatState is ChatUiState.Success) {
            val messages = currentChatState.messages
            if (messages.isNotEmpty()) {
                lazyListState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (val state = chatState) {
                            is ChatUiState.Success -> state.otherUserName
                            else -> "Chat"
                        },
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3)
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp)),
                        placeholder = {
                            Text("Digite sua mensagem...", color = Color.Gray)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = {
                            if (messageText.trim().isNotEmpty()) {
                                chatViewModel.sendMessage(matchId, messageText.trim())
                                messageText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar mensagem"
                        )
                    }
                }
            }
        }
    ) { padding ->
        when (val state = chatState) {
            is ChatUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2196F3))
                }
            }

            is ChatUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Erro ao carregar chat",
                            color = Color.Red,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { chatViewModel.loadMessages(matchId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Tentar Novamente", color = Color.White)
                        }
                    }
                }
            }

            is ChatUiState.Success -> {
                val messages = state.messages
                val otherUserName = state.otherUserName
                val currentUserId = state.currentUserId

                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎉", fontSize = 48.sp, modifier = Modifier.padding(bottom = 16.dp))
                            Text(
                                text = "Vocês se conectaram!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2196F3),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Envie a primeira mensagem para $otherUserName",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(messages) { message ->
                            MessageItem(
                                message = message,
                                isFromCurrentUser = message.senderId == currentUserId
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageItem(
    message: ChatMessage,
    isFromCurrentUser: Boolean
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeText = timeFormat.format(message.timestamp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                            bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isFromCurrentUser) Color(0xFF2196F3) else Color(0xFFF5F5F5)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.message,
                    color = if (isFromCurrentUser) Color.White else Color.Black,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = timeText,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
