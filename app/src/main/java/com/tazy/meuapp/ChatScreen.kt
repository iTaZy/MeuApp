package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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
    var mostrarParticipantes by remember { mutableStateOf(false) } // DECLARAÇÃO CORRIGIDA
    var listaParticipantes by remember { mutableStateOf<List<Participante>>(emptyList()) }
    var carregandoParticipantes by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nomeGrupo.ifBlank { "Grupo" }, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        carregandoParticipantes = true
                        viewModel.carregarParticipantes(grupoId) { lista ->
                            listaParticipantes = lista
                            mostrarParticipantes = true
                            carregandoParticipantes = false
                        }
                    }) {
                        Icon(Icons.Default.Info, contentDescription = "Ver participantes", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2196F3))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFECEFF1))
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(mensagens) { msg ->
                    val isUser = msg.remetenteId == authUid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .background(
                                    color = if (isUser) Color(0xFFC8E6C9) else Color.White,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp)
                        ) {
                            if (!isUser) Text(msg.remetenteNome, fontSize = 12.sp, color = Color(0xFF37474F))
                            Text(msg.texto, fontSize = 16.sp, color = Color.Black)
                            Text(
                                text = formatarHora(msg.timestamp),
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = novaMensagem,
                    onValueChange = { novaMensagem = it },
                    placeholder = { Text("Digite uma mensagem...") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
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
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (novaMensagem.isNotBlank()) {
                            viewModel.enviarMensagem(grupoId, novaMensagem.trim())
                            novaMensagem = ""
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color(0xFF2196F3))
                }
            }
        }
    }

    // Dialog de Participantes
    if (mostrarParticipantes) {
        AlertDialog(
            onDismissRequest = { mostrarParticipantes = false },
            title = { Text("Participantes (${listaParticipantes.size})") },
            text = {
                if (carregandoParticipantes) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Carregando participantes...")
                    }
                } else {
                    LazyColumn {
                        items(listaParticipantes) { participante ->
                            Text(
                                text = "• ${participante.nome}",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarParticipantes = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}

private fun formatarHora(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
    }


