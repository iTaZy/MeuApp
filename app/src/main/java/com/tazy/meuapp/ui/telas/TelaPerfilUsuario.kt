package com.tazy.meuapp.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.tazy.meuapp.viewmodel.PerfilUiState
import com.tazy.meuapp.viewmodel.PerfilUsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPerfilUsuario(
    navController: NavController,
    userId: String,
    viewModel: PerfilUsuarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // FOTO DE PERFIL
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

                        // NOME E IDADE
                        val nomeExibicao = if (state.idade > 0) "${state.nome}, ${state.idade}" else state.nome
                        Text(
                            text = nomeExibicao,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )

                        // TAGS BÁSICAS (Sexualidade / Signo)
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

                        // SOBRE MIM (BIO)
                        if (state.bio.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(FieldBg)
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                    .padding(20.dp)
                            ) {
                                Column {
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

                        // INTERESSES
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(FieldBg)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .padding(20.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("Gostos e Interesses", color = AccentPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))

                                // Junta tudo numa lista só para exibir nas tags
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