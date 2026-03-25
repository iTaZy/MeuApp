package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

    var menuExpandido by remember { mutableStateOf(false) }
    var mostrarApelidoDialog by remember { mutableStateOf(false) }
    var novoApelido by remember { mutableStateOf("") }

    val BgDeep = Color(0xFF060B10)
    val BgMid = Color(0xFF0B1422)
    val AccentCyan = Color(0xFF4DD9E8)
    val AccentPurple = Color(0xFF8B5CF6)
    val AccentBlue = Color(0xFF3B82F6)
    val TextPrimary = Color(0xFFE8F4FF)
    val TextSecondary = Color(0xFF8BA8C0)
    val FieldBg = Color(0xFF0D1A2A).copy(alpha = 0.75f)
    val FieldBorder = Color(0xFFFFFFFF).copy(alpha = 0.18f)

    val gradientButton = Brush.linearGradient(listOf(AccentPurple, AccentBlue, AccentCyan))

    LaunchedEffect(matchId) {
        chatViewModel.loadMessages(matchId)
        chatViewModel.markMessagesAsRead(matchId)
    }

    LaunchedEffect(chatState) {
        val currentChatState = chatState
        if (currentChatState is ChatUiState.Success) {
            val messages = currentChatState.messages
            if (messages.isNotEmpty()) {
                lazyListState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgMid, Color(0xFF050A0F))))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (val state = chatState) {
                                    is ChatUiState.Success -> state.otherUserName
                                    else -> "Chat"
                                },
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = AccentCyan)
                            }
                        },
                        actions = {
                            IconButton(onClick = { menuExpandido = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Opções", tint = AccentCyan)
                            }
                            DropdownMenu(
                                expanded = menuExpandido,
                                onDismissRequest = { menuExpandido = false },
                                modifier = Modifier.background(BgMid)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Mudar Apelido", color = TextPrimary) },
                                    onClick = {
                                        menuExpandido = false
                                        novoApelido = if (chatState is ChatUiState.Success) (chatState as ChatUiState.Success).otherUserName else ""
                                        mostrarApelidoDialog = true
                                    }
                                )
                                // 👇 A MÁGICA DE NAVEGAÇÃO PRO PERFIL ACONTECE AQUI
                                DropdownMenuItem(
                                    text = { Text("Ver Perfil", color = TextPrimary) },
                                    onClick = {
                                        menuExpandido = false
                                        if (chatState is ChatUiState.Success) {
                                            val otherId = (chatState as ChatUiState.Success).otherUserId
                                            if (otherId.isNotBlank()) {
                                                navController.navigate("perfil/$otherId")
                                            }
                                        }
                                    }
                                )

                                val isArchived = if (chatState is ChatUiState.Success) (chatState as ChatUiState.Success).isArchived else false

                                if (isArchived) {
                                    DropdownMenuItem(
                                        text = { Text("Desarquivar Conversa", color = AccentCyan) },
                                        onClick = {
                                            menuExpandido = false
                                            chatViewModel.desarquivarConversa(matchId) {
                                                navController.popBackStack()
                                            }
                                        }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text("Arquivar Conversa", color = Color(0xFFFF5252)) },
                                        onClick = {
                                            menuExpandido = false
                                            chatViewModel.arquivarConversa(matchId) {
                                                navController.popBackStack()
                                            }
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = FieldBorder)
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgMid)
                        .border(width = 1.dp, color = FieldBorder, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(FieldBg),
                            placeholder = { Text("Digite sua mensagem...", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan, unfocusedBorderColor = Color.Transparent,
                                cursorColor = AccentCyan, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                if (messageText.trim().isNotEmpty()) {
                                    chatViewModel.sendMessage(matchId, messageText.trim())
                                    messageText = ""
                                }
                            },
                            modifier = Modifier.size(50.dp).clip(CircleShape).background(gradientButton)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        ) { padding ->
            when (val state = chatState) {
                is ChatUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentCyan)
                    }
                }
                is ChatUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Erro ao carregar chat", color = Color(0xFFFF5252), fontSize = 16.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(32.dp)).background(gradientButton)) {
                                TextButton(onClick = { chatViewModel.loadMessages(matchId) }) {
                                    Text("Tentar Novamente", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                is ChatUiState.Success -> {
                    val messages = state.messages
                    val otherUserName = state.otherUserName
                    val currentUserId = state.currentUserId

                    if (messages.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎉", fontSize = 56.sp, modifier = Modifier.padding(bottom = 16.dp))
                                Text("Vocês se conectaram!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AccentCyan, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Envie a primeira mensagem para $otherUserName", fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                            }
                        }
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(messages) { message ->
                                MessageItem(message = message, isFromCurrentUser = message.senderId == currentUserId)
                            }
                        }
                    }
                }
            }
        }

        // Dialog de Alterar Apelido
        if (mostrarApelidoDialog) {
            AlertDialog(
                onDismissRequest = { mostrarApelidoDialog = false },
                containerColor = BgMid,
                title = { Text("Mudar Apelido", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = novoApelido,
                        onValueChange = { novoApelido = it },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(FieldBg),
                        placeholder = { Text("Digite um apelido...", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan, unfocusedBorderColor = FieldBorder,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            cursorColor = AccentCyan, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (novoApelido.isNotBlank()) {
                            chatViewModel.salvarApelido(matchId, novoApelido.trim()) {
                                mostrarApelidoDialog = false
                            }
                        }
                    }) {
                        Text("Salvar", color = AccentCyan, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarApelidoDialog = false }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun MessageItem(message: ChatMessage, isFromCurrentUser: Boolean) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeText = timeFormat.format(message.timestamp)

    val AccentCyan = Color(0xFF4DD9E8)
    val AccentBlue = Color(0xFF3B82F6)
    val TextPrimary = Color(0xFFE8F4FF)
    val TextSecondary = Color(0xFF8BA8C0)
    val FieldBg = Color(0xFF0D1A2A).copy(alpha = 0.75f)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start) {
        Column(horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isFromCurrentUser) 16.dp else 4.dp, bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp))
                    .background(if (isFromCurrentUser) Brush.linearGradient(listOf(AccentBlue.copy(0.8f), AccentCyan.copy(0.8f))) else androidx.compose.ui.graphics.SolidColor(FieldBg))
                    .border(1.dp, if (isFromCurrentUser) Color.Transparent else Color(0xFFFFFFFF).copy(alpha = 0.1f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isFromCurrentUser) 16.dp else 4.dp, bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp).widthIn(max = 280.dp)
            ) {
                Text(text = message.message, color = TextPrimary, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = timeText, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 4.dp))
        }
    }
}