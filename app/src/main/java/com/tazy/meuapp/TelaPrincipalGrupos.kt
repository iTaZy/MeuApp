package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun TelaPrincipalGrupos(navController: NavController) {
    val viewModel: TelaPrincipalViewModel = viewModel()

    val state by viewModel.state.collectAsState()

    if (state.codigoCondominio.isBlank() && state.carregando) {
        LoadingState()
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("criarGrupo") },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, contentDescription = "Novo grupo") }
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

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator(color = Color(0xFF2196F3)) }
}

@Composable
private fun ErrorState(viewModel: TelaPrincipalViewModel, error: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Erro: $error", color = Color.Red)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.atualizarDados() }) { Text("Tentar novamente") }
        }
    }
}

@Composable
private fun ContentState(
    navController: NavController,
    viewModel: TelaPrincipalViewModel,
    state: TelaPrincipalState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Olá, ${state.nomeUsuario}!",
                    fontSize = 20.sp,
                    color = Color(0xFF2196F3)
                )
                Text(
                    "Condomínio: ${state.codigoCondominio}",
                    color = Color.Gray
                )
            }

            // Botão de Edição
            Button(
                onClick = {
                    navController.navigate("loginPrimeiraVez") {
                        popUpTo("telaPrincipal") { saveState = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Editar Perfil")
            }
        }

        // Grupos Recomendados
        Text(
            text = "Grupos Recomendados",
            modifier = Modifier.padding(16.dp),
            color = Color(0xFF2196F3)
        )

        LazyRow(
            modifier = Modifier.padding(start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.gruposRecomendados) { grupo ->
                GrupoRecomendadoCard(
                    grupo = grupo,
                    navController = navController,
                    viewModel = viewModel // ← Adicionar aqui
                )
            }
        }

        // Seus Grupos
        Text(
            text = "Meus Grupos",
            modifier = Modifier.padding(16.dp),
            color = Color(0xFF2196F3)
        )

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // ← espaçamento de 8dp entre os grupos
        ) {
            items(state.chatsAtivos) { chat ->
                GrupoAtivoCard(
                    chat = chat,
                    navController = navController,
                    viewModel = viewModel
                )
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
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Entrar no grupo") },
            text = { Text("Deseja entrar no grupo ${grupo.nome}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.entrarNoGrupo(grupo.id)
                    showDialog = false
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar") } }
        )
    }
    Card(
        Modifier.width(280.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB3E5FC)),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = grupo.nome,
                    modifier = Modifier.weight(1f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                RatingBar(grupo.relevancia)
            }
            Spacer(Modifier.height(8.dp))
            Text(grupo.descricao, fontSize = 14.sp, color = Color.DarkGray, maxLines = 2)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2196F3))
                Spacer(Modifier.width(4.dp))
                Text("${grupo.membrosCount} membros", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { showDialog = true }, Modifier.fillMaxWidth()) { Text("Entrar no grupo") }
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
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Sair do grupo") },
            text = { Text("Deseja sair do grupo ${chat.nome}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.sairDoGrupo(chat.id)
                    showDialog = false
                }) { Text("Sair" ) }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar") } }
        )
    }
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB3E5FC)),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).clickable { navController.navigate("chat/${chat.id}") }) {
                Text(chat.nome, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(chat.ultimaMensagem, fontSize = 14.sp, color = Color.Gray, maxLines = 1)
            }
            Badge(Modifier.padding(end = 8.dp), containerColor = Color(0xFF2196F3)) {
                Text(chat.mensagensNaoLidas.toString(), color = Color.White)
            }
            IconButton(onClick = { showDialog = true }) { Icon(Icons.Default.ExitToApp, contentDescription = "") }
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
                modifier = Modifier.size(16.dp),
                tint = if (index < rating) Color(0xFFFFC107) else Color.LightGray
            )
        }
    }
}
