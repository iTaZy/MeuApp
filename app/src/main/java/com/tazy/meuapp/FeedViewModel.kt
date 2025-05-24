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

    fun startListeningFeed(codigoCondominio: String) {
        if (codigoCondominio.isBlank()) return

        loading.value = true
        stopListeningFeed()

        val userId = auth.currentUser?.uid ?: run {
            loading.value = false
            return
        }

        listenerRegistration = db.collection("posts")
            .whereEqualTo("codigoCondominio", codigoCondominio)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                loading.value = false

                if (error != null) {
                    Log.e("FeedViewModel", "Erro no listener: ${error.message}")
                    return@addSnapshotListener
                }

                snapshots?.let { docs ->
                    val rawPosts = docs.documents.mapNotNull { doc ->
                        doc.toObject(Post::class.java)?.copy(id = doc.id)
                    }

                    checkLikesForPosts(userId, rawPosts)
                }
            }
    }

    private fun checkLikesForPosts(userId: String, rawPosts: List<Post>) {
        viewModelScope.launch {
            val postsWithLikes = rawPosts.map { post ->
                val liked = try {
                    db.collection("posts")
                        .document(post.id)
                        .collection("likes")
                        .document(userId)
                        .get()
                        .await()
                        .exists()
                } catch (e: Exception) {
                    false
                }
                post.copy(likedByUser = liked)
            }
            _posts.value = postsWithLikes
        }
    }

    fun stopListeningFeed() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    fun criarPost(texto: String, nomeUsuario: String, codigoCondominio: String, onResult: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return onResult(false)

        val post = hashMapOf(
            "authorId" to user.uid,
            "authorName" to nomeUsuario,
            "text" to texto,
            "timestamp" to Timestamp.now(),
            "likesCount" to 0,
            "codigoCondominio" to codigoCondominio
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

    // NOVO: função para excluir post
    fun excluirPost(postId: String, onResult: (Boolean) -> Unit) {
        db.collection("posts").document(postId).delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    suspend fun getCodigoCondominio(userId: String): String? {
        return try {
            val snapshot = db.collection("usuarios").document(userId).get().await()
            snapshot.getString("codigoCondominio")
        } catch (e: Exception) {
            Log.e("FeedViewModel", "Erro ao buscar código do condomínio", e)
            null
        }
    }
}
