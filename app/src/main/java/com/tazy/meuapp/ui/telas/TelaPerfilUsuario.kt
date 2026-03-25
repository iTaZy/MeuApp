package com.tazy.meuapp.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.tazy.meuapp.TelaPrincipalViewModel
import com.tazy.meuapp.model.CabecalhoUsuario
import com.tazy.meuapp.model.RodapeUsuario
import com.tazy.meuapp.ui.components.PostItem
import com.tazy.meuapp.viewmodel.PerfilUiState
import com.tazy.meuapp.viewmodel.PerfilUsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPerfilUsuario(
    navController: NavController,
    userId: String,
    viewModel: PerfilUsuarioViewModel = hiltViewModel(),
    userViewModel: TelaPrincipalViewModel = hiltViewModel() // NOVO: Para pegar os dados do cabeçalho
) {
    val uiState by viewModel.uiState.collectAsState()
    val userState by userViewModel.state.collectAsState() // Estado para o cabeçalho

    // Verifica se o perfil sendo visualizado é o do próprio usuário logado
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isOwnProfile = currentUserId == userId

    // Paleta KLANCORE
    val BgDeep = Color(0xFF060B10)
    val BgMid = Color(0xFF0B1422)
    val AccentCyan = Color(0xFF4DD9E8)
    val AccentPurple = Color(0xFF8B5CF6)
    val AccentBlue = Color(0xFF3B82F6)
    val TextPrimary = Color(0xFFE8F4FF)
    val TextSecondary = Color(0xFF8BA8C0)
    val FieldBg = Color(0xFF0D1A2A).copy(alpha = 0.75f)

    val gradientBorder = Brush.linearGradient(listOf(AccentCyan, AccentPurple))

    LaunchedEffect(userId) {
        viewModel.carregarPerfil(userId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgMid, Color(0xFF050A0F))))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (isOwnProfile) {
                    // Se for o próprio perfil, mostra o cabeçalho padrão do app
                    CabecalhoUsuario(state = userState, navController = navController)
                } else {
                    // Se for o perfil de outra pessoa, mostra a barra com botão de voltar
                    TopAppBar(
                        title = { Text("Perfil", color = TextPrimary, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = AccentCyan)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            bottomBar = {
                // Se for o próprio perfil, mostra o rodapé na aba "Perfil"
                if (isOwnProfile) {
                    RodapeUsuario(navController = navController, selected = "Perfil")
                }
            }
        ) { padding ->
            when (val state = uiState) {
                is PerfilUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentCyan)
                    }
                }
                is PerfilUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color(0xFFFF5252), fontSize = 16.sp)
                    }
                }
                is PerfilUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(bottom = 40.dp) // Espaço extra no final
                    ) {

                        // --- CABEÇALHO DO PERFIL ---
                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(CircleShape)
                                    .background(Brush.radialGradient(listOf(AccentCyan.copy(0.15f), FieldBg)))
                                    .border(2.dp, gradientBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(70.dp), tint = TextSecondary.copy(alpha = 0.5f))
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            val nomeExibicao = if (state.idade > 0) "${state.nome}, ${state.idade}" else state.nome
                            Text(
                                text = nomeExibicao,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )

                            if (state.sexualidade.isNotEmpty() || state.signo.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.padding(top = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (state.sexualidade.isNotEmpty()) TagPerfil(text = state.sexualidade, color = AccentCyan)
                                    if (state.signo.isNotEmpty()) TagPerfil(text = state.signo, color = AccentPurple)
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }

                        // --- SOBRE MIM ---
                        item {
                            if (state.bio.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(FieldBg)
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                        .padding(20.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                                        Text("Sobre Mim", color = AccentCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = state.bio,
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // --- GOSTOS E INTERESSES ---
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(FieldBg)
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                    .padding(20.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                                    Text("Gostos e Interesses", color = AccentPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    val todosGostos = mutableListOf<String>()
                                    if (state.interessePrincipal.isNotBlank()) todosGostos.add(state.interessePrincipal)
                                    todosGostos.addAll(state.subcategorias.filter { it.isNotBlank() })
                                    todosGostos.addAll(state.outrosInteresses.filter { it.isNotBlank() })

                                    if (todosGostos.isEmpty()) {
                                        Text("Esta pessoa ainda não definiu seus interesses.", color = TextSecondary, fontSize = 14.sp, fontStyle = FontStyle.Italic)
                                    } else {
                                        @OptIn(ExperimentalLayoutApi::class)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            todosGostos.forEach { interesse ->
                                                TagPerfil(text = interesse, color = AccentBlue, isFilled = true)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(40.dp))
                        }

                        // --- TÍTULO DAS PUBLICAÇÕES ---
                        item {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "Publicações",
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                if (state.posts.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                        Text("Nenhuma publicação no radar.", color = TextSecondary, fontSize = 15.sp)
                                    }
                                }
                            }
                        }

                        // --- LISTA DE POSTS DO USUÁRIO ---
                        items(state.posts) { post ->
                            PostItem(
                                post = post,
                                onLikeClick = { viewModel.toggleCurtirPost(post) },
                                onDeleteClick = { viewModel.excluirPost(post.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagPerfil(text: String, color: Color, isFilled: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFilled) color.copy(alpha = 0.2f) else Color.Transparent)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text = text.replaceFirstChar { it.uppercase() }, fontSize = 13.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}