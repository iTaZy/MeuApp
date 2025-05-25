package com.meuapp.ui.telas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tazy.meuapp.R
import com.tazy.meuapp.viewmodel.ConexoesUiState

import com.tazy.meuapp.viewmodel.ConexoesViewModel

@Composable
fun TelaConexoes(
    viewModel: ConexoesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Título
        Text(
            text = "Bem-vindo às Conexões",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2196F3),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        when (uiState) {
            is ConexoesUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is ConexoesUiState.Error -> {
                val errorMessage = (uiState as ConexoesUiState.Error).message
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is ConexoesUiState.Empty -> {
                Text(
                    text = "Não há mais perfis para mostrar.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is ConexoesUiState.Success -> {
                val profile = (uiState as ConexoesUiState.Success).currentProfile

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profile.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "${profile.name} | ${profile.age}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = profile.bio,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botões de interação
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão de Voltar
                    IconButton(
                        onClick = { viewModel.dislikeProfile() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(BorderStroke(2.dp, Color(0xFF1565C0)), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_seta_voltar),
                            contentDescription = "Voltar",
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Botão Criar Vínculo
                    Button(
                        onClick = { viewModel.likeProfile() },
                        modifier = Modifier
                            .height(48.dp)
                            .width(180.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Text(text = "Criar Vínculo", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Botão Compartilhar
                    IconButton(
                        onClick = { /* ação futura */ },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(BorderStroke(2.dp, Color(0xFF1565C0)), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_seta_compartilhar),
                            contentDescription = "Compartilhar",
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}