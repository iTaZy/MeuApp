package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tazy.meuapp.model.CabecalhoUsuario
import com.tazy.meuapp.model.RodapeUsuario
import com.tazy.meuapp.model.MatchesUiState
import com.tazy.meuapp.viewmodel.MatchesViewModel
import com.tazy.meuapp.ui.components.ItemConversa
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaLobbyPrincipal(
    navController: NavController,
    matchesViewModel: MatchesViewModel = hiltViewModel()
) {
    val viewModel: TelaPrincipalViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val matchesState by matchesViewModel.uiState.collectAsState()

    if (state.codigoCondominio.isBlank() && state.carregando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF2196F3))
        }
        return
    }

    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CabecalhoUsuario(state = state, navController = navController)
        },
        bottomBar = {
            RodapeUsuario(navController = navController, selected = "Lobby")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Bem-vindo ao Lobby",
                color = Color(0xFF2196F3),
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF2196F3),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Conversas") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Arquivadas") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (val currentState = matchesState) {
                is MatchesUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2196F3))
                    }
                }

                is MatchesUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Erro ao carregar conversas",
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { matchesViewModel.loadMatches() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3)
                                )
                            ) {
                                Text("Tentar Novamente", color = Color.White)
                            }
                        }
                    }
                }

                is MatchesUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "💬",
                                fontSize = 48.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "Nenhuma conversa ainda",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Quando você fizer conexões, suas conversas aparecerão aqui!",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigate("TelaConexoes") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3)
                                )
                            ) {
                                Text("Fazer Conexões", color = Color.White)
                            }
                        }
                    }
                }

                is MatchesUiState.Success -> {
                    if (selectedTabIndex == 0) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            items(currentState.matches) { match ->
                                val otherUserName = match.getOtherUserName(
                                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                )

                                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val displayTime = match.lastMessageTime?.let { timeFormat.format(it) }
                                    ?: timeFormat.format(match.timestamp)

                                val displayMessage = if (match.lastMessage.isNotEmpty()) {
                                    match.lastMessage
                                } else {
                                    "Vocês se conectaram! Envie a primeira mensagem."
                                }

                                ItemConversa(
                                    nome = otherUserName,
                                    mensagem = displayMessage,
                                    horario = displayTime,
                                    onClick = {
                                        navController.navigate("chat/${match.id}")
                                    }
                                )
                            }
                        }
                    } else {
                        // Aba de Arquivadas - Por enquanto vazia
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📁",
                                    fontSize = 48.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Text(
                                    text = "Nenhuma conversa arquivada",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
