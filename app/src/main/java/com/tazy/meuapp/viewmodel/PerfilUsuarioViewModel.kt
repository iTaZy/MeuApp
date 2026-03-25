package com.tazy.meuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tazy.meuapp.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class PerfilUiState {
    object Loading : PerfilUiState()
    data class Success(
        val nome: String,
        val idade: Int,
        val bio: String,
        val sexualidade: String,
        val signo: String,
        val interessePrincipal: String,
        val subcategorias: List<String>,
        val outrosInteresses: List<String>,
        val posts: List<Post> // NOVO: Lista de publicações do usuário
    ) : PerfilUiState()
    data class Error(val message: String) : PerfilUiState()
}

@HiltViewModel
class PerfilUsuarioViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth // NOVO: Para sabermos quem somos na hora do Like
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Loading)
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun carregarPerfil(userId: String) {
        viewModelScope.launch {
            _uiState.value = PerfilUiState.Loading
            try {
                // 1. Busca os dados do usuário
                val document = firestore.collection("usuarios").document(userId).get().await()

                if (document.exists()) {
                    val nome = document.getString("nome") ?: "Usuário"
                    val idade = document.getLong("idade")?.toInt() ?: 0
                    val bio = document.getString("bio") ?: ""
                    val sexualidade = document.getString("sexualidade") ?: ""
                    val signo = document.getString("signo") ?: ""
                    val interessePrincipal = document.getString("interesse") ?: ""
                    val subcategorias = document.get("subcategorias") as? List<String> ?: emptyList()
                    val outrosInteresses = document.get("outrosInteresses") as? List<String> ?: emptyList()

                    // 2. Busca os posts desse usuário
                    val postsSnapshot = firestore.collection("posts")
                        .whereEqualTo("authorId", userId)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get()
                        .await()

                    val currentUserId = auth.currentUser?.uid ?: ""

                    val postsDoUsuario = postsSnapshot.documents.mapNotNull { doc ->
                        val post = doc.toObject(Post::class.java)?.copy(id = doc.id)
                        if (post != null) {
                            // Verifica se EU (usuário logado) curti o post dessa pessoa
                            val liked = try {
                                firestore.collection("posts").document(post.id)
                                    .collection("likes").document(currentUserId)
                                    .get().await().exists()
                            } catch (e: Exception) { false }

                            post.copy(likedByUser = liked)
                        } else null
                    }

                    _uiState.value = PerfilUiState.Success(
                        nome = nome,
                        idade = idade,
                        bio = bio,
                        sexualidade = sexualidade,
                        signo = signo,
                        interessePrincipal = interessePrincipal,
                        subcategorias = subcategorias,
                        outrosInteresses = outrosInteresses,
                        posts = postsDoUsuario
                    )
                } else {
                    _uiState.value = PerfilUiState.Error("Usuário não encontrado.")
                }
            } catch (e: Exception) {
                _uiState.value = PerfilUiState.Error("Erro ao carregar o perfil.")
            }
        }
    }

    // Funcionalidade de Like direto na tela de perfil
    fun toggleCurtirPost(post: Post) {
        val currentUserId = auth.currentUser?.uid ?: return
        val likeRef = firestore.collection("posts").document(post.id).collection("likes").document(currentUserId)
        val postRef = firestore.collection("posts").document(post.id)

        // Atualização instantânea na tela (Otimista)
        val currentState = _uiState.value
        if (currentState is PerfilUiState.Success) {
            val updatedPosts = currentState.posts.map {
                if (it.id == post.id) {
                    it.copy(
                        likedByUser = !post.likedByUser,
                        likesCount = if (post.likedByUser) it.likesCount - 1 else it.likesCount + 1
                    )
                } else it
            }
            _uiState.value = currentState.copy(posts = updatedPosts)
        }

        // Atualização no banco de dados
        if (post.likedByUser) {
            likeRef.delete().addOnSuccessListener {
                postRef.update("likesCount", FieldValue.increment(-1))
            }
        } else {
            likeRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    likeRef.set(mapOf("likedAt" to Timestamp.now())).addOnSuccessListener {
                        postRef.update("likesCount", FieldValue.increment(1))
                    }
                }
            }
        }
    }

    fun excluirPost(postId: String) {
        firestore.collection("posts").document(postId).delete().addOnSuccessListener {
            val currentState = _uiState.value
            if (currentState is PerfilUiState.Success) {
                _uiState.value = currentState.copy(posts = currentState.posts.filter { it.id != postId })
            }
        }
    }
}