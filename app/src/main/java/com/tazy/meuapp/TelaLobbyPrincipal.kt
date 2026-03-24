package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

    // Paleta Klancore
    val BgDeep = Color(0xFF060B10)
    val BgMid = Color(0xFF0B1422)
    val AccentCyan = Color(0xFF4DD9E8)
    val AccentPurple = Color(0xFF8B5CF6)
    val AccentBlue = Color(0xFF3B82F6)
    val TextPrimary = Color(0xFFE8F4FF)
    val TextSecondary = Color(0xFF8BA8C0)
    val gradientButton = Brush.linearGradient(listOf(AccentPurple, AccentBlue, AccentCyan))

    if (state.codigoCondominio.isBlank() && state.carregando) {
        Box(
            modifier = Modifier.fillMaxSize().background(BgDeep),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentCyan)
        }
        return
    }

    var selectedTabIndex by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgMid, Color(0xFF050A0F))))
    ) {
        Scaffold(
            containerColor = Color.Transparent, // Essencial para o gradiente de fundo aparecer
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
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Lobby de Conversas",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = AccentCyan,
                    divider = { HorizontalDivider(color = TextSecondary.copy(alpha = 0.2f)) },
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = AccentCyan,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text("Conversas",
                                color = if(selectedTabIndex == 0) AccentCyan else TextSecondary,
                                fontWeight = if(selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text("Arquivadas",
                                color = if(selectedTabIndex == 1) AccentCyan else TextSecondary,
                                fontWeight = if(selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (val currentState = matchesState) {
                    is MatchesUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AccentCyan)
                        }
                    }

                    is MatchesUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Erro ao carregar conversas",
                                    color = Color(0xFFFF5252),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(32.dp))
                                        .background(gradientButton)
                                        .fillMaxWidth(0.6f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TextButton(onClick = { matchesViewModel.loadMatches() }) {
                                        Text("Tentar Novamente", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    is MatchesUiState.Empty -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "💬",
                                    fontSize = 56.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Text(
                                    text = "Nenhuma conversa ainda",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Quando você fizer conexões, suas conversas aparecerão aqui!",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(32.dp))
                                        .background(gradientButton)
                                        .fillMaxWidth(0.7f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TextButton(onClick = { navController.navigate("TelaConexoes") }) {
                                        Text("Fazer Conexões", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    is MatchesUiState.Success -> {
                        if (selectedTabIndex == 0) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
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
                            // Aba de Arquivadas
                            Box(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "📁",
                                        fontSize = 56.sp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    Text(
                                        text = "Nenhuma conversa arquivada",
                                        fontSize = 16.sp,
                                        color = TextSecondary,
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
}