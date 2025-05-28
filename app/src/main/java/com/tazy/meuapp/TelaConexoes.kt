package com.meuapp.ui.telas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tazy.meuapp.TelaPrincipalState
import com.tazy.meuapp.model.CabecalhoUsuario
import com.tazy.meuapp.model.RodapeUsuario
import com.tazy.meuapp.viewmodel.ConexoesUiState
import com.tazy.meuapp.viewmodel.ConexoesViewModel

@Composable
fun TelaConexoes(
    navController: NavController,
    viewModel: ConexoesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Estados para carregar dados do usuário
    var nomeUsuario by remember { mutableStateOf("Usuário") }
    var codigoCondominio by remember { mutableStateOf("Condomínio") }
    var carregandoDados by remember { mutableStateOf(true) }

    // Cores do tema
    val azulPrimario = Color(0xFF2196F3)
    val azulEscuro = Color(0xFF1565C0)
    val fundoCinza = Color(0xFFF5F5F5)

    // Carregar dados do usuário
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            firestore.collection("usuarios")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    nomeUsuario = document.getString("nome") ?: currentUser.displayName ?: "Usuário"
                    codigoCondominio = document.getString("codigoCondominio") ?: "Condomínio"
                    carregandoDados = false
                }
                .addOnFailureListener {
                    nomeUsuario = currentUser.displayName ?: "Usuário"
                    codigoCondominio = "Condomínio"
                    carregandoDados = false
                }
        } else {
            carregandoDados = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Cabeçalho do usuário
        if (!carregandoDados) {
            CabecalhoUsuario(
                state = TelaPrincipalState(
                    nomeUsuario = nomeUsuario,
                    codigoCondominio = codigoCondominio
                ),
                navController = navController
            )
        }

        // Conteúdo principal
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Título das Conexões
            Text(
                text = "Bem-vindo às Conexões",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = azulPrimario,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            when (uiState) {
                is ConexoesUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = azulPrimario)
                    }
                }

                is ConexoesUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState as ConexoesUiState.Error).message,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is ConexoesUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🎉",
                                fontSize = 48.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "Não há mais perfis para mostrar.",
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                is ConexoesUiState.Success -> {
                    val profile = (uiState as ConexoesUiState.Success).currentProfile

                    // Card do perfil
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(480.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Placeholder da imagem de fundo
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFE3F2FD),
                                                Color(0xFFBBDEFB),
                                                azulPrimario.copy(alpha = 0.3f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Ícone de pessoa como placeholder
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.size(120.dp),
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            // Gradiente overlay na parte inferior
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.8f)
                                            )
                                        )
                                    )
                            )

                            // Informações do perfil
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "${profile.name} | ${profile.age}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                if (profile.bio.isNotEmpty()) {
                                    Text(
                                        text = profile.bio,
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botões de ação
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botão de Voltar/Rejeitar
                        IconButton(
                            onClick = { viewModel.dislikeProfile() },
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(BorderStroke(2.dp, azulEscuro), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = "Voltar",
                                tint = azulEscuro,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Botão principal - Criar Vínculo
                        Button(
                            onClick = { viewModel.likeProfile() },
                            modifier = Modifier
                                .height(52.dp)
                                .width(200.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = azulEscuro),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "Criar Vínculo",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        // Botão Compartilhar
                        IconButton(
                            onClick = { /* ação futura */ },
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(BorderStroke(2.dp, azulEscuro), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartilhar",
                                tint = azulEscuro,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Rodapé do usuário
        RodapeUsuario(navController = navController, selected = "Conexões")
    }
}