package com.tazy.meuapp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.FirebaseAuth
import com.tazy.meuapp.model.CabecalhoUsuario
import com.tazy.meuapp.model.RodapeUsuario
import com.tazy.meuapp.ui.components.PostItem
import com.tazy.meuapp.model.Post
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaFeedNoticias(
    navController: NavController,
    viewModel: FeedViewModel = hiltViewModel(),
    userViewModel: TelaPrincipalViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val comentarios by viewModel.comentarios.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val state by userViewModel.state.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var novoPostTexto by remember { mutableStateOf("") }

    // Estado do BottomSheet de Comentários
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var postSelecionadoParaComentar by remember { mutableStateOf<Post?>(null) }
    var novoComentarioTexto by remember { mutableStateOf("") }

    // Cores KLANCORE
    val BgDeep = Color(0xFF060B10)
    val BgMid = Color(0xFF0B1422)
    val AccentCyan = Color(0xFF4DD9E8)
    val AccentPurple = Color(0xFF8B5CF6)
    val AccentBlue = Color(0xFF3B82F6)
    val TextPrimary = Color(0xFFE8F4FF)
    val TextSecondary = Color(0xFF8BA8C0)
    val FieldBg = Color(0xFF0D1A2A).copy(alpha = 0.75f)
    val FieldBorder = Color(0xFFFFFFFF).copy(alpha = 0.18f)

    val gradientButton = Brush.linearGradient(listOf(AccentPurple, AccentBlue, AccentCyan))

    LaunchedEffect(user) {
        viewModel.startListeningFeed()
    }

    val refreshState = rememberSwipeRefreshState(isRefreshing = loading)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgMid, Color(0xFF050A0F))))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CabecalhoUsuario(state = state, navController = navController)
            },
            bottomBar = {
                RodapeUsuario(navController = navController, selected = "Feed")
            },
            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(gradientButton)
                        .clickable { showDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Novo Post",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { paddingValues ->

            // DIALOGO DE NOVO POST
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    containerColor = BgMid,
                    title = { Text("Novo Post", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = {
                        OutlinedTextField(
                            value = novoPostTexto,
                            onValueChange = { novoPostTexto = it },
                            placeholder = { Text("O que você está pensando?", color = TextSecondary) },
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(FieldBg),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan, unfocusedBorderColor = FieldBorder,
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                                cursorColor = AccentCyan, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent
                            ),
                            minLines = 3
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val userName = state.nomeUsuario ?: "Usuário"
                                if (novoPostTexto.isNotBlank()) {
                                    viewModel.criarPost(novoPostTexto, userName) { success ->
                                        if (success) {
                                            showDialog = false
                                            novoPostTexto = ""
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Publicar", color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancelar", color = TextSecondary)
                        }
                    }
                )
            }

            // LISTA DE POSTS
            SwipeRefresh(
                state = refreshState,
                onRefresh = { viewModel.startListeningFeed() },
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Feed de Notícias",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    if (loading && posts.isEmpty()) {
                        LinearProgressIndicator(color = AccentCyan, modifier = Modifier.fillMaxWidth())
                    }

                    if (posts.isEmpty() && !loading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🪐", fontSize = 56.sp, modifier = Modifier.padding(bottom = 16.dp))
                                Text("Nenhum post no seu radar.", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Seja o primeiro a compartilhar algo com a galera que curte as mesmas coisas que você!", color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(posts) { post ->
                                PostItem(
                                    post = post,
                                    onLikeClick = { viewModel.toggleCurtirPost(post) },
                                    onCommentClick = {
                                        postSelecionadoParaComentar = post
                                        viewModel.abrirComentarios(post.id)
                                    },
                                    onDeleteClick = { viewModel.excluirPost(post.id) { } }
                                )
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM SHEET DOS COMENTÁRIOS
        if (postSelecionadoParaComentar != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    postSelecionadoParaComentar = null
                    viewModel.fecharComentarios()
                    novoComentarioTexto = ""
                },
                sheetState = sheetState,
                containerColor = BgMid, // Fundo Neon Dark
                dragHandle = { BottomSheetDefaults.DragHandle(color = TextSecondary) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f) // Ocupa 70% da tela
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Comentários", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(bottom = 16.dp))

                    // LISTA DE COMENTARIOS
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (comentarios.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("Seja o primeiro a comentar!", color = TextSecondary, fontSize = 14.sp)
                                }
                            }
                        } else {
                            items(comentarios) { comentario ->
                                val timeFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                                val timeText = comentario.timestamp?.toDate()?.let { timeFormat.format(it) } ?: ""

                                Column(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(FieldBg).padding(12.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(comentario.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccentCyan)
                                        Text(timeText, fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(comentario.text, fontSize = 14.sp, color = TextPrimary)
                                }
                            }
                        }
                    }

                    // CAIXA DE DIGITAR O COMENTÁRIO
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, top = 8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = novoComentarioTexto,
                            onValueChange = { novoComentarioTexto = it },
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(FieldBg),
                            placeholder = { Text("Adicione um comentário...", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan, unfocusedBorderColor = Color.Transparent,
                                cursorColor = AccentCyan, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                if (novoComentarioTexto.trim().isNotEmpty()) {
                                    val userName = state.nomeUsuario ?: "Usuário"
                                    viewModel.adicionarComentario(postSelecionadoParaComentar!!.id, novoComentarioTexto.trim(), userName)
                                    novoComentarioTexto = ""
                                }
                            },
                            modifier = Modifier.size(50.dp).clip(CircleShape).background(gradientButton)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }
    }
}