package com.meuapp.ui.telas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
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
import com.tazy.meuapp.R

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
                                text = "Você já viu todos os perfis disponíveis no seu condomínio.",
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.reloadProfiles() },
                                colors = ButtonDefaults.buttonColors(containerColor = azulPrimario),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Recarregar",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                is ConexoesUiState.Success -> {
                    val profile = (uiState as ConexoesUiState.Success).currentProfile
                    val currentIndex = (uiState as ConexoesUiState.Success).currentIndex
                    val totalProfiles = (uiState as ConexoesUiState.Success).totalProfiles

                    Column {
                        // Indicador de posição
                        Text(
                            text = "${currentIndex + 1} de $totalProfiles",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

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

                                    // Sexualidade e Signo (se disponíveis)
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        if (profile.sexualidade.isNotEmpty()) {
                                            Text(
                                                text = profile.sexualidade,
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.8f),
                                                modifier = Modifier
                                                    .background(
                                                        Color.White.copy(alpha = 0.2f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

                                        if (profile.signo.isNotEmpty()) {
                                            Text(
                                                text = profile.signo,
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.8f),
                                                modifier = Modifier
                                                    .background(
                                                        Color.White.copy(alpha = 0.2f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    if (profile.bio.isNotEmpty()) {
                                        Text(
                                            text = profile.bio,
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.9f),
                                            lineHeight = 20.sp,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }

                                    // Interesses (se disponíveis)
                                    if (profile.interesses.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Mostrar apenas os primeiros 3 interesses para não sobrecarregar
                                        val interessesToShow = profile.interesses.take(3)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            interessesToShow.forEach { interesse ->
                                                Text(
                                                    text = interesse,
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    modifier = Modifier
                                                        .background(
                                                            azulPrimario.copy(alpha = 0.7f),
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }

                                            // Indicador se há mais interesses
                                            if (profile.interesses.size > 3) {
                                                Text(
                                                    text = "+${profile.interesses.size - 3}",
                                                    fontSize = 11.sp,
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    modifier = Modifier
                                                        .background(
                                                            Color.White.copy(alpha = 0.2f),
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
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
                            // Botão de Voltar
                            IconButton(
                                onClick = { viewModel.previousProfile() },
                                enabled = viewModel.canGoBack(),
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (viewModel.canGoBack()) Color.White
                                        else Color.Gray.copy(alpha = 0.3f)
                                    )
                                    .border(
                                        BorderStroke(
                                            2.dp,
                                            if (viewModel.canGoBack()) azulEscuro
                                            else Color.Gray
                                        ),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_seta_voltar),
                                    contentDescription = "Voltar",
                                    tint = if (viewModel.canGoBack()) azulEscuro else Color.Gray,
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

                            // Botão de Avançar
                            IconButton(
                                onClick = { viewModel.nextProfile() },
                                enabled = viewModel.canGoForward(),
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (viewModel.canGoForward()) Color.White
                                        else Color.Gray.copy(alpha = 0.3f)
                                    )
                                    .border(
                                        BorderStroke(
                                            2.dp,
                                            if (viewModel.canGoForward()) azulEscuro
                                            else Color.Gray
                                        ),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_seta_avancar),
                                    contentDescription = "Avançar",
                                    tint = if (viewModel.canGoForward()) azulEscuro else Color.Gray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Rodapé do usuário
        RodapeUsuario(navController = navController, selected = "Conexões")
    }
}