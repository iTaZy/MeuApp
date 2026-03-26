package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll // Novo Import
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState // Novo Import
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tazy.meuapp.model.CabecalhoUsuario
import com.tazy.meuapp.model.RodapeUsuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPrincipalGrupos(navController: NavController) {
    val viewModel: TelaPrincipalViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    // Paleta KLANCORE
    val BgDeep = Color(0xFF060B10)
    val BgMid = Color(0xFF0B1422)
    val AccentCyan = Color(0xFF4DD9E8)
    val AccentPurple = Color(0xFF8B5CF6)
    val AccentBlue = Color(0xFF3B82F6)

    val gradientButton = Brush.linearGradient(listOf(AccentPurple, AccentBlue, AccentCyan))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgMid, Color(0xFF050A0F))))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { CabecalhoUsuario(state = state, navController = navController) },
            bottomBar = { RodapeUsuario(navController = navController, selected = "Grupos") },
            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(gradientButton)
                        .clickable { navController.navigate("criarGrupo") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Novo grupo", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    state.carregando -> LoadingState()
                    state.erro != null -> ErrorState(viewModel, state.erro)
                    else -> ContentState(navController, viewModel, state)
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF4DD9E8))
    }
}

@Composable
private fun ErrorState(viewModel: TelaPrincipalViewModel, error: String?) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Erro: $error", color = Color(0xFFFF5252), textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.atualizarDados() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4DD9E8))
            ) { Text("Tentar novamente", color = Color(0xFF060B10), fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ContentState(
    navController: NavController,
    viewModel: TelaPrincipalViewModel,
    state: TelaPrincipalState
) {
    val AccentCyan = Color(0xFF4DD9E8)
    val TextPrimary = Color(0xFFE8F4FF)

    Column(modifier = Modifier.fillMaxSize()) {

        // Grupos Recomendados
        Text(
            text = "Grupos Recomendados",
            modifier = Modifier.padding(16.dp),
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            modifier = Modifier.padding(start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.gruposRecomendados) { grupo ->
                GrupoRecomendadoCard(grupo = grupo, navController = navController, viewModel = viewModel)
            }
            item { Spacer(modifier = Modifier.width(16.dp)) }
        }

        // Seus Grupos
        Text(
            text = "Meus Grupos",
            modifier = Modifier.padding(top = 24.dp, start = 16.dp, bottom = 16.dp),
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(state.chatsAtivos) { chat ->
                GrupoAtivoCard(chat = chat, navController = navController, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun GrupoRecomendadoCard(
    grupo: GrupoRecomendado,
    navController: NavController,
    viewModel: TelaPrincipalViewModel
) {
    var showDialog by remember { mutableStateOf(false) }

    val AccentCyan = Color(0xFF4DD9E8)
    val TextPrimary = Color(0xFFE8F4FF)
    val TextSecondary = Color(0xFF8BA8C0)
    val FieldBg = Color(0xFF0D1A2A).copy(alpha = 0.75f)

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFF0B1422),
            title = { Text("Entrar no grupo", color = TextPrimary) },
            text = { Text("Deseja entrar no grupo ${grupo.nome}?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.entrarNoGrupo(grupo.id)
                    showDialog = false
                }) { Text("Confirmar", color = AccentCyan, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar", color = TextSecondary) }
            }
        )
    }

    Box(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(FieldBg)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = grupo.nome,
                    modifier = Modifier.weight(1f),
                    fontSize = 18.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                RatingBar(grupo.relevancia)
            }
            Spacer(Modifier.height(8.dp))
            Text(grupo.descricao, fontSize = 14.sp, color = TextSecondary, maxLines = 2)

            // 👇 TAGS DE INTERESSE 👇
            if (grupo.interesses.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()), // Permite arrastar pro lado
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grupo.interesses.forEach { interesse ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF3B82F6).copy(alpha = 0.2f)) // AccentBlue transparente
                                .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = interesse.replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp,
                                color = AccentCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            // 👆 FIM DAS TAGS 👆

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp), tint = AccentCyan)
                Spacer(Modifier.width(6.dp))
                Text("${grupo.membrosCount} membros", fontSize = 13.sp, color = TextSecondary)
            }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Participar", color = AccentCyan, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GrupoAtivoCard(
    chat: Chat,
    navController: NavController,
    viewModel: TelaPrincipalViewModel
) {
    var showDialog by remember { mutableStateOf(false) }

    val AccentCyan = Color(0xFF4DD9E8)
    val TextPrimary = Color(0xFFE8F4FF)
    val TextSecondary = Color(0xFF8BA8C0)
    val FieldBg = Color(0xFF0D1A2A).copy(alpha = 0.75f)

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFF0B1422),
            title = { Text("Sair do grupo", color = TextPrimary) },
            text = { Text("Deseja mesmo sair do grupo ${chat.nome}?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.sairDoGrupo(chat.id)
                    showDialog = false
                }) { Text("Sair", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar", color = TextSecondary) }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FieldBg)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .clickable { navController.navigate("chatGrupo/${chat.id}") }
    ){
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(chat.nome, fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (chat.ultimaMensagem.isBlank()) "Toque para enviar a primeira mensagem!" else chat.ultimaMensagem,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            if (chat.mensagensNaoLidas > 0) {
                Box(
                    modifier = Modifier.padding(end = 12.dp).size(24.dp).clip(CircleShape).background(AccentCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Text(chat.mensagensNaoLidas.toString(), color = Color(0xFF060B10), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Sair", tint = Color(0xFFFF5252).copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun RatingBar(rating: Int) {
    Row {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (index < rating) Color(0xFFFFC107) else Color.White.copy(alpha = 0.2f)
            )
        }
    }
}