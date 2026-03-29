package com.tazy.meuapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import com.tazy.meuapp.model.Post
import com.tazy.meuapp.model.Comment
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
        val id: String,
        val nome: String,
        val fotoPerfil: String? = null, // ✨ Guarda a foto do perfil
        val idade: Int,
        val bio: String,
        val sexualidade: String,
        val signo: String,
        val interessePrincipal: String,
        val subcategorias: List<String>,
        val outrosInteresses: List<String>,
        val posts: List<Post>
    ) : PerfilUiState()
    data class Error(val message: String) : PerfilUiState()
}

@HiltViewModel
class PerfilUsuarioViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Loading)
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    // Lógica de Comentários
    private var commentsListener: ListenerRegistration? = null
    private val _comentarios = MutableStateFlow<List<Comment>>(emptyList())
    val comentarios = _comentarios.asStateFlow()

    fun carregarPerfil(userId: String) {
        viewModelScope.launch {
            _uiState.value = PerfilUiState.Loading
            try {
                val document = firestore.collection("usuarios").document(userId).get().await()

                if (document.exists()) {
                    val nome = document.getString("nome") ?: "Usuário"
                    val fotoPerfil = document.getString("fotoPerfil") // ✨ Puxa o link da foto do banco!
                    val idade = document.getLong("idade")?.toInt() ?: 0
                    val bio = document.getString("bio") ?: ""
                    val sexualidade = document.getString("sexualidade") ?: ""
                    val signo = document.getString("signo") ?: ""
                    val interessePrincipal = document.getString("interesse") ?: ""
                    val subcategorias = document.get("subcategorias") as? List<String> ?: emptyList()
                    val outrosInteresses = document.get("outrosInteresses") as? List<String> ?: emptyList()

                    val postsSnapshot = firestore.collection("posts")
                        .whereEqualTo("authorId", userId)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get()
                        .await()

                    val currentUserId = auth.currentUser?.uid ?: ""

                    val postsDoUsuario = postsSnapshot.documents.mapNotNull { doc ->
                        val post = doc.toObject(Post::class.java)?.copy(id = doc.id)
                        if (post != null) {
                            val liked = try {
                                firestore.collection("posts").document(post.id)
                                    .collection("likes").document(currentUserId)
                                    .get().await().exists()
                            } catch (e: Exception) { false }

                            post.copy(likedByUser = liked)
                        } else null
                    }

                    _uiState.value = PerfilUiState.Success(
                        id = document.id, // ✨ ID passado corretamente
                        nome = nome,
                        fotoPerfil = fotoPerfil, // ✨ Foto passada corretamente para a tela
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
                _uiState.value = PerfilUiState.Error("Erro: ${e.message}")
            }
        }
    }

    fun toggleCurtirPost(post: Post) {
        val currentUserId = auth.currentUser?.uid ?: return
        val likeRef = firestore.collection("posts").document(post.id).collection("likes").document(currentUserId)
        val postRef = firestore.collection("posts").document(post.id)

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

    // --- LÓGICA DE COMENTÁRIOS ---
    fun abrirComentarios(postId: String) {
        commentsListener?.remove()
        _comentarios.value = emptyList()

        commentsListener = firestore.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val commentsList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                }
                _comentarios.value = commentsList
            }
    }

    fun fecharComentarios() {
        commentsListener?.remove()
        commentsListener = null
        _comentarios.value = emptyList()
    }

    fun adicionarComentario(postId: String, texto: String, nomeUsuario: String) {
        val user = auth.currentUser ?: return

        val commentData = hashMapOf(
            "authorId" to user.uid,
            "authorName" to nomeUsuario,
            "text" to texto,
            "timestamp" to Timestamp.now()
        )

        // Atualização Otimista no Perfil
        val currentState = _uiState.value
        if (currentState is PerfilUiState.Success) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == postId) {
                    post.copy(commentsCount = post.commentsCount + 1)
                } else post
            }
            _uiState.value = currentState.copy(posts = updatedPosts)
        }

        firestore.collection("posts").document(postId).collection("comments").add(commentData)
            .addOnSuccessListener {
                firestore.collection("posts").document(postId).update("commentsCount", FieldValue.increment(1))
            }
            .addOnFailureListener { e ->
                Log.e("PerfilViewModel", "Erro ao salvar comentário: ", e)
                // Reverte na tela se der erro de conexão
                if (currentState is PerfilUiState.Success) {
                    _uiState.value = currentState
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        fecharComentarios()
    }
}