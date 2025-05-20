package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun TelaPrincipal(navController: NavController) {
    val viewModel: TelaPrincipalViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("criarGrupo") },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo grupo")
            }
        }
    ) { paddingValues ->
        when {
            state.carregando -> LoadingState()
            state.erro != null -> ErrorState(viewModel, state.erro)
            else -> ContentState(navController, viewModel, state)
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF2196F3))
    }
}

@Composable
private fun ErrorState(viewModel: TelaPrincipalViewModel, error: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Erro: $error", color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.atualizarDados() }) {
                Text("Tentar novamente")
            }
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
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Olá, ${state.nomeUsuario}!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3)
            )
            IconButton(onClick = { viewModel.atualizarDados() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grupos recomendados
        Text(
            "Grupos recomendados",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(state.gruposRecomendados) { grupo ->
                GrupoRecomendadoCard(grupo, navController, viewModel)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Seus grupos
        Text(
            "Seus grupos",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.chatsAtivos) { chat ->
                GrupoAtivoCard(chat, navController, viewModel)
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
                Button(
                    onClick = {
                        viewModel.entrarNoGrupo(grupo.id)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .width(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFB3E5FC)
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    grupo.nome,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                RatingBar(rating = grupo.relevancia)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                grupo.descricao,
                fontSize = 14.sp,
                color = Color.DarkGray,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.People,
                    contentDescription = "Membros",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "${grupo.membrosCount} membros",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text("Entrar no grupo", color = Color.White)
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

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Sair do grupo") },
            text = { Text("Deseja realmente sair do grupo ${chat.nome}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sairDoGrupo(chat.id)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Sair")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFB3E5FC)
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { navController.navigate("chat/${chat.id}") }
            ) {
                Text(
                    chat.nome,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    chat.ultimaMensagem,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            if (chat.mensagensNaoLidas > 0) {
                Badge(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(chat.mensagensNaoLidas.toString())
                }
            }

            IconButton(
                onClick = { showDialog = true }
            ) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = "Sair do grupo",
                    tint = Color.Red
                )
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
                contentDescription = "Rating",
                tint = if (index < rating) Color(0xFFFFC107) else Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}