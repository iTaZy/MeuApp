package com.tazy.meuapp

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tazy.meuapp.model.CabecalhoUsuario
import com.tazy.meuapp.model.RodapeUsuario
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaConexoes(navController: NavController) {
    val viewModel: TelaPrincipalViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CabecalhoUsuario(state = state, navController = navController)
        },
        bottomBar = {
            RodapeUsuario(navController = navController, selected = "Conexões")
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Lista de perfis fictícios (substitua por dados reais do seu app)
            val profiles = listOf(
                Profile(
                    id = "1",
                    name = "Ana Silva",
                    imageUrl = "https://randomuser.me/api/portraits/women/42.jpg",
                    age = 28,
                    bio = "Mora no bloco B, ama animais e jardinagem"
                ),
                Profile(
                    id = "2",
                    name = "Carlos Oliveira",
                    imageUrl = "https://randomuser.me/api/portraits/men/32.jpg",
                    age = 35,
                    bio = "Corredor de fim de semana, sempre no parque aos sábados"
                ),
                Profile(
                    id = "3",
                    name = "Mariana Costa",
                    imageUrl = "https://randomuser.me/api/portraits/women/63.jpg",
                    age = 31,
                    bio = "Organiza eventos no condomínio, adora cozinhar"
                )
            )

            var currentProfileIndex by remember { mutableStateOf(0) }

            if (currentProfileIndex < profiles.size) {
                val currentProfile = profiles[currentProfileIndex]

                var offsetX by remember { mutableStateOf(0f) }
                var offsetY by remember { mutableStateOf(0f) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Conheça seus vizinhos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currentProfile.imageUrl)
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
                                    text = "${currentProfile.name}, ${currentProfile.age}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Text(
                                    text = currentProfile.bio,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Botão de dislike
                        Button(
                            onClick = {
                                // Animação de deslize para esquerda
                                offsetX = -1000f
                                offsetY = 0f
                                // Avança para o próximo perfil
                                currentProfileIndex++
                            },
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Red
                            ),
                            border = BorderStroke(2.dp, Color.Red)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dislike",
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        // Botão de like
                        Button(
                            onClick = {
                                // Animação de deslize para direita
                                offsetX = 1000f
                                offsetY = 0f
                                // Avança para o próximo perfil
                                currentProfileIndex++
                            },
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Green
                            ),
                            border = BorderStroke(2.dp, Color.Green)
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Like",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            } else {
                // Todos os perfis foram vistos
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Você viu todos os perfis disponíveis!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "Volte mais tarde para conhecer novos vizinhos",
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

data class Profile(
    val id: String,
    val name: String,
    val imageUrl: String,
    val age: Int,
    val bio: String
)