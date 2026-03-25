package com.tazy.meuapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.tazy.meuapp.model.Post

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private var listenerRegistration: ListenerRegistration? = null
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts = _posts.asStateFlow()
    val loading = MutableStateFlow(false)

    fun startListeningFeed() {
        loading.value = true
        stopListeningFeed()

        val userId = auth.currentUser?.uid ?: run {
            loading.value = false
            return
        }

        viewModelScope.launch {
            try {
                // 1. Busca os gostos do usuário logado
                val currentUserDoc = db.collection("usuarios").document(userId).get().await()
                val meuInteresse = currentUserDoc.getString("interesse")
                val minhasSubcategorias = currentUserDoc.get("subcategorias") as? List<String> ?: emptyList()
                val meusOutrosInteresses = currentUserDoc.get("outrosInteresses") as? List<String> ?: emptyList()

                val meusGostos = mutableSetOf<String>()
                meuInteresse?.let { if (it.isNotBlank()) meusGostos.add(it) }
                meusGostos.addAll(minhasSubcategorias.filter { it.isNotBlank() })
                meusGostos.addAll(meusOutrosInteresses.filter { it.isNotBlank() })

                // 2. Ouve todos os posts do banco em tempo real
                listenerRegistration = db.collection("posts")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshots, error ->
                        loading.value = false

                        if (error != null) {
                            Log.e("FeedViewModel", "Erro no listener: ${error.message}")
                            return@addSnapshotListener
                        }

                        snapshots?.let { docs ->
                            val rawPosts = docs.documents.mapNotNull { doc ->
                                val post = doc.toObject(Post::class.java)?.copy(id = doc.id)

                                if (post != null) {
                                    val isMe = post.authorId == userId
                                    val gostosPost = post.authorInterests.toSet()
                                    val temInteresseEmComum = meusGostos.intersect(gostosPost).isNotEmpty()

                                    // A REGRA DE OURO: Mostra se for meu post OU se tivermos gostos em comum
                                    if (isMe || temInteresseEmComum) post else null
                                } else null
                            }

                            checkLikesForPosts(userId, rawPosts)
                        }
                    }
            } catch (e: Exception) {
                loading.value = false
            }
        }
    }

    private fun checkLikesForPosts(userId: String, rawPosts: List<Post>) {
        viewModelScope.launch {
            val postsWithLikes = rawPosts.map { post ->
                val liked = try {
                    db.collection("posts").document(post.id).collection("likes").document(userId).get().await().exists()
                } catch (e: Exception) { false }
                post.copy(likedByUser = liked)
            }
            _posts.value = postsWithLikes
        }
    }

    fun stopListeningFeed() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    fun criarPost(texto: String, nomeUsuario: String, onResult: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return onResult(false)

        viewModelScope.launch {
            try {
                // Pega os interesses do criador na hora de postar
                val currentUserDoc = db.collection("usuarios").document(user.uid).get().await()
                val meuInteresse = currentUserDoc.getString("interesse")
                val minhasSubcategorias = currentUserDoc.get("subcategorias") as? List<String> ?: emptyList()
                val meusOutrosInteresses = currentUserDoc.get("outrosInteresses") as? List<String> ?: emptyList()

                val meusGostos = mutableSetOf<String>()
                meuInteresse?.let { if (it.isNotBlank()) meusGostos.add(it) }
                meusGostos.addAll(minhasSubcategorias.filter { it.isNotBlank() })
                meusGostos.addAll(meusOutrosInteresses.filter { it.isNotBlank() })

                val post = hashMapOf(
                    "authorId" to user.uid,
                    "authorName" to nomeUsuario,
                    "text" to texto,
                    "timestamp" to Timestamp.now(),
                    "likesCount" to 0,
                    "authorInterests" to meusGostos.toList() // Salva os interesses junto com o post
                )

                db.collection("posts").add(post).await()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun toggleCurtirPost(post: Post) {
        val userId = auth.currentUser?.uid ?: return
        val likeRef = db.collection("posts").document(post.id).collection("likes").document(userId)
        val postRef = db.collection("posts").document(post.id)

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

    fun excluirPost(postId: String, onResult: (Boolean) -> Unit) {
        db.collection("posts").document(postId).delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}