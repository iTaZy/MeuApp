package com.tazy.meuapp

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.FirebaseAuth
import com.tazy.meuapp.ui.components.PostItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaFeedNoticias(
    navController: NavController,
    viewModel: FeedViewModel = hiltViewModel(),
    userViewModel: TelaPrincipalViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val state by userViewModel.state.collectAsState()

    var userCondominiumCode by remember { mutableStateOf<String?>(null) }

    var showDialog by remember { mutableStateOf(false) }
    var novoPostTexto by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            val code = viewModel.getCodigoCondominio(uid)
            userCondominiumCode = code
            code?.let { viewModel.startListeningFeed(it) }
        }
    }

    val refreshState = rememberSwipeRefreshState(isRefreshing = loading)

    Scaffold(
        topBar = {
            CabecalhoUsuario(state = state, navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Post")
            }
        }
    ) { paddingValues ->

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Novo Post") },
                text = {
                    OutlinedTextField(
                        value = novoPostTexto,
                        onValueChange = { novoPostTexto = it },
                        label = { Text("Digite seu post") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val userName = state.nomeUsuario ?: "Usuário"
                            val codigoCondominio = userCondominiumCode ?: ""
                            if (novoPostTexto.isNotBlank() && codigoCondominio.isNotBlank()) {
                                viewModel.criarPost(novoPostTexto, userName, codigoCondominio) { success ->
                                    if (success) {
                                        showDialog = false
                                        novoPostTexto = ""
                                    } else {
                                        Log.e("TelaFeedNoticias", "Erro ao criar post")
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Publicar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        SwipeRefresh(
            state = refreshState,
            onRefresh = { userCondominiumCode?.let { viewModel.startListeningFeed(it) } },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                if (loading && posts.isEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                when {
                    userCondominiumCode == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Você ainda não está vinculado a um condomínio.")
                        }
                    }
                    posts.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhuma publicação encontrada.")
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(posts) { post ->
                                PostItem(
                                    post = post,
                                    onLikeClick = { liked ->
                                        viewModel.toggleCurtirPost(post)
                                    },
                                    onDeleteClick = {
                                        viewModel.excluirPost(post.id) { success ->
                                            if (!success) {
                                                Log.e("TelaFeedNoticias", "Erro ao excluir post")
                                                // Aqui pode mostrar Snackbar/Toast se quiser
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
