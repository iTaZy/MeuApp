package com.tazy.meuapp

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaFeedNoticias(
    navController: NavHostController,
    telaPrincipalViewModel: TelaPrincipalViewModel = viewModel(),
    viewModel: FeedViewModel = viewModel()
) {
    val context = LocalContext.current
    val posts by viewModel.posts.collectAsState()
    val state by telaPrincipalViewModel.state.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var textoPost by remember { mutableStateOf("") }
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startListeningFeed()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopListeningFeed()
        }
    }

    Scaffold(
        topBar = {
            CabecalhoUsuario(state = state, navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Criar Post")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
        ) {
            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(posts) { post ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = post.authorName,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = post.text,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(
                                text = post.timestamp?.toDate()?.let {
                                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                                } ?: "",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val icon = if (post.likedByUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
                                val tint = if (post.likedByUser) Color.Red else Color.Gray
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Curtir",
                                    tint = tint,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            viewModel.toggleCurtirPost(post)
                                        }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "${post.likesCount}")
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (textoPost.isBlank()) {
                                Toast.makeText(context, "Escreva algo antes de publicar", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            viewModel.criarPost(textoPost.trim(), state.nomeUsuario) { sucesso ->
                                if (sucesso) {
                                    textoPost = ""
                                    showDialog = false
                                    Toast.makeText(context, "Post criado!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Erro ao criar post", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !loading
                    ) {
                        Text("Publicar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Novo Post") },
                text = {
                    OutlinedTextField(
                        value = textoPost,
                        onValueChange = { textoPost = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text("O que você quer compartilhar?") },
                        maxLines = 5,
                        enabled = !loading
                    )
                },
                shape = RoundedCornerShape(12.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}
