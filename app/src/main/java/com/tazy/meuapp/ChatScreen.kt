package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.tazy.meuapp.ChatViewModel.Participante
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    grupoId: String,
    viewModel: ChatViewModel = viewModel()
) {
    var nomeGrupo by remember { mutableStateOf("") }
    var mostrarParticipantes by remember { mutableStateOf(false) }
    var listaParticipantes by remember { mutableStateOf<List<Participante>>(emptyList()) }
    var carregandoParticipantes by remember { mutableStateOf(false) }

    // Paleta KLANCORE
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

    LaunchedEffect(grupoId) {
        viewModel.carregarDadosGrupo(grupoId) { nome ->
            nomeGrupo = nome
        }
        viewModel.iniciar(grupoId)
    }

    val state by viewModel.state.collectAsState()
    val mensagens = state.mensagens
    val authUid = FirebaseAuth.getInstance().currentUser?.uid
    var novaMensagem by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    LaunchedEffect(mensagens.size) {
        delay(100)
        listState.animateScrollToItem(mensagens.lastIndex.coerceAtLeast(0))
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
                        title = { Text(nomeGrupo.ifBlank { "Grupo" }, color = TextPrimary, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = AccentCyan)
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                carregandoParticipantes = true
                                mostrarParticipantes = true
                                viewModel.carregarParticipantes(grupoId) { lista ->
                                    listaParticipantes = lista
                                    carregandoParticipantes = false
                                }
                            }) {
                                Icon(Icons.Default.Info, contentDescription = "Ver participantes", tint = AccentCyan)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = novaMensagem,
                            onValueChange = { novaMensagem = it },
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(FieldBg),
                            placeholder = { Text("Digite sua mensagem...", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan, unfocusedBorderColor = Color.Transparent,
                                cursorColor = AccentCyan, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (novaMensagem.isNotBlank()) {
                                    viewModel.enviarMensagem(grupoId, novaMensagem.trim())
                                    novaMensagem = ""
                                    focusManager.clearFocus()
                                }
                            })
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                if (novaMensagem.isNotBlank()) {
                                    viewModel.enviarMensagem(grupoId, novaMensagem.trim())
                                    novaMensagem = ""
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier.size(50.dp).clip(CircleShape).background(gradientButton)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (mensagens.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhuma mensagem ainda. Dê um oi!", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(mensagens) { msg ->
                        val isUser = msg.remetenteId == authUid

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 16.dp
                                        ))
                                        .background(if (isUser) Brush.linearGradient(listOf(AccentBlue.copy(0.8f), AccentCyan.copy(0.8f))) else androidx.compose.ui.graphics.SolidColor(FieldBg))
                                        .border(1.dp, if (isUser) Color.Transparent else Color(0xFFFFFFFF).copy(alpha = 0.1f), RoundedCornerShape(
                                            topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isUser) 16.dp else 4.dp, bottomEnd = if (isUser) 4.dp else 16.dp
                                        ))
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                        .widthIn(max = 280.dp)
                                ) {
                                    Column {
                                        if (!isUser) {
                                            Text(
                                                text = msg.remetenteNome,
                                                fontSize = 13.sp,
                                                color = AccentCyan,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                        }
                                        Text(text = msg.texto, color = TextPrimary, fontSize = 15.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatarHora(msg.timestamp),
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog de Participantes com visual Klancore
        if (mostrarParticipantes) {
            AlertDialog(
                onDismissRequest = { mostrarParticipantes = false },
                containerColor = BgMid,
                title = { Text("Participantes (${listaParticipantes.size})", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    if (carregandoParticipantes) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AccentCyan)
                            Spacer(Modifier.height(16.dp))
                            Text("Buscando a galera...", color = TextSecondary)
                        }
                    } else {
                        LazyColumn {
                            items(listaParticipantes) { participante ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentPurple)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = participante.nome, color = TextPrimary, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { mostrarParticipantes = false }) {
                        Text("Fechar", color = AccentCyan, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

private fun formatarHora(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}