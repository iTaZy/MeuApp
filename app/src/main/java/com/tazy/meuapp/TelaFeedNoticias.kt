package com.tazy.meuapp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.TextFieldDefaults

// Modelo de dados do Post
data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,
    val likesCount: Int = 0,
    val likedByUser: Boolean = false
)

class FeedViewModel : androidx.lifecycle.ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    var posts by mutableStateOf<List<Post>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    fun startListeningFeed() {
        loading = true
        val userId = auth.currentUser?.uid ?: return

        listenerRegistration = db.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    loading = false
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val batchPosts = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(Post::class.java)?.copy(id = doc.id)
                    }

                    val tasks = batchPosts.map { post ->
                        db.collection("posts")
                            .document(post.id)
                            .collection("likes")
                            .document(userId)
                            .get()
                            .continueWith { task ->
                                val liked = task.result?.exists() ?: false
                                post.copy(likedByUser = liked)
                            }
                    }

                    com.google.android.gms.tasks.Tasks.whenAllSuccess<Post>(tasks)
                        .addOnSuccessListener { postsResult ->
                            posts = postsResult
                            loading = false
                        }
                        .addOnFailureListener {
                            posts = batchPosts
                            loading = false
                        }
                }
            }
    }

    fun stopListeningFeed() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    fun criarPost(texto: String, onResult: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false)
            return
        }
        val post = hashMapOf(
            "authorId" to user.uid,
            "authorName" to (user.displayName ?: "Usuário"),
            "text" to texto,
            "timestamp" to Timestamp.now(),
            "likesCount" to 0
        )
        db.collection("posts").add(post)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun toggleCurtirPost(post: Post) {
        val userId = auth.currentUser?.uid ?: return
        val likeRef = db.collection("posts").document(post.id).collection("likes").document(userId)
        val postRef = db.collection("posts").document(post.id)

        if (post.likedByUser) {
            likeRef.delete().addOnSuccessListener {
                postRef.update("likesCount", com.google.firebase.firestore.FieldValue.increment(-1))
            }
        } else {
            likeRef.set(mapOf("likedAt" to Timestamp.now())).addOnSuccessListener {
                postRef.update("likesCount", com.google.firebase.firestore.FieldValue.increment(1))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaFeedNoticias(
    navController: NavHostController,
    telaPrincipalViewModel: TelaPrincipalViewModel = viewModel(), // viewModel que tem o state para o cabeçalho
    viewModel: FeedViewModel = viewModel()
) {
    val context = LocalContext.current
    val posts by remember { derivedStateOf { viewModel.posts } }
    var textoPost by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val state by telaPrincipalViewModel.state.collectAsState()

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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = textoPost,
                onValueChange = { textoPost = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("O que você quer compartilhar?") },
                maxLines = 4,
                enabled = !loading,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (textoPost.isBlank()) {
                        Toast.makeText(context, "Escreva algo antes de publicar", Toast.LENGTH_SHORT).show()
                    } else {
                        loading = true
                        viewModel.criarPost(textoPost.trim()) { sucesso ->
                            loading = false
                            if (sucesso) {
                                textoPost = ""
                                Toast.makeText(context, "Post criado!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Erro ao criar post", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End),
                enabled = !loading
            ) {
                Text("Publicar")
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
    }
}
