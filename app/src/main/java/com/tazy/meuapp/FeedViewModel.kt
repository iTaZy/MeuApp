package com.tazy.meuapp

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,
    val likesCount: Int = 0,
    val likedByUser: Boolean = false,
    val codigoCondominio: String = "" // Added condominium code field
)

class FeedViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts = _posts.asStateFlow()

    var loading = MutableStateFlow(false)
        private set

    fun startListeningFeed() {
        val userId = auth.currentUser?.uid ?: return
        loading.value = true

        // First get the user's condominium code
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val codigoCondominio = userDoc.getString("codigoCondominio") ?: run {
                    loading.value = false
                    return@addOnSuccessListener
                }

                // Then listen to posts from the same condominium
                listenerRegistration = db.collection("posts")
                    .whereEqualTo("codigoCondominio", codigoCondominio)
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshots, error ->
                        if (error != null) {
                            loading.value = false
                            return@addSnapshotListener
                        }

                        if (snapshots != null) {
                            val rawPosts = snapshots.documents.mapNotNull { doc ->
                                doc.toObject(Post::class.java)?.copy(id = doc.id)
                            }

                            val tasks = rawPosts.map { post ->
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
                                .addOnSuccessListener { posts ->
                                    _posts.value = posts
                                    loading.value = false
                                }
                                .addOnFailureListener {
                                    _posts.value = rawPosts
                                    loading.value = false
                                }
                        }
                    }
            }
            .addOnFailureListener {
                loading.value = false
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
            "codigoCondominio" to codigoCondominio // Added condominium code
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
            // Remove o like
            likeRef.delete().addOnSuccessListener {
                postRef.update("likesCount", FieldValue.increment(-1))
            }
        } else {
            // Adiciona o like (só permite se não tiver curtido antes)
            likeRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    likeRef.set(mapOf("likedAt" to Timestamp.now())).addOnSuccessListener {
                        postRef.update("likesCount", FieldValue.increment(1))
                    }
                }
            }
        }
    }

    // Add this function to get the user's condominium code
    suspend fun getCodigoCondominio(userId: String): String? {
        return try {
            val snapshot = db.collection("usuarios").document(userId).get().await()
            snapshot.getString("codigoCondominio")
        } catch (e: Exception) {
            null
        }
    }
}