// MatchesViewModel.kt
package com.tazy.meuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tazy.meuapp.model.Match
import com.tazy.meuapp.model.MatchesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class MatchesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MatchesUiState>(MatchesUiState.Loading)
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadMatches()
    }

    fun loadMatches() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = MatchesUiState.Error("Usuário não autenticado")
            return
        }

        _uiState.value = MatchesUiState.Loading

        // Usar SnapshotListener para atualizar em Tempo Real!
        firestore.collection("matches")
            .whereArrayContains("participants", currentUser.uid)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = MatchesUiState.Error("Erro ao carregar matches: ${error.localizedMessage}")
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    _uiState.value = MatchesUiState.Empty
                    return@addSnapshotListener
                }

                viewModelScope.launch {
                    try {
                        val matches = mutableListOf<Match>()

                        for (document in snapshot.documents) {
                            val participants = document.get("participants") as? List<String> ?: continue
                            val user1Id = participants.getOrNull(0) ?: continue
                            val user2Id = participants.getOrNull(1) ?: continue

                            // Busca nomes
                            val user1Doc = firestore.collection("usuarios").document(user1Id).get().await()
                            val user2Doc = firestore.collection("usuarios").document(user2Id).get().await()

                            val user1Name = user1Doc.getString("nome") ?: "Usuário"
                            val user2Name = user2Doc.getString("nome") ?: "Usuário"

                            val match = Match(
                                id = document.id,
                                user1Id = user1Id,
                                user2Id = user2Id,
                                user1Name = user1Name,
                                user2Name = user2Name,
                                timestamp = document.getDate("timestamp") ?: Date(),
                                lastMessage = document.getString("lastMessage") ?: "",
                                lastMessageTime = document.getDate("lastMessageTime"),
                                isActive = document.getBoolean("isActive") ?: true,
                                lastMessageSenderId = document.getString("lastMessageSenderId") ?: "",
                                isLastMessageRead = document.getBoolean("isLastMessageRead") ?: true
                            )
                            matches.add(match)
                        }

                        _uiState.value = MatchesUiState.Success(matches)

                    } catch (e: Exception) {
                        _uiState.value = MatchesUiState.Error("Erro ao processar matches.")
                    }
                }
            }
    }

    // Função para criar um match quando duas pessoas se curtem
    suspend fun createMatchIfExists(likedUserId: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        try {
            // Verificar se o outro usuário também curtiu
            val mutualLikeQuery = firestore.collection("conexoes")
                .whereEqualTo("userId", likedUserId)
                .whereEqualTo("likedUserId", currentUser.uid)
                .whereEqualTo("liked", true)
                .get()
                .await()

            if (mutualLikeQuery.documents.isNotEmpty()) {
                // Existe match mútuo, criar o documento de match
                val matchData = hashMapOf(
                    "participants" to listOf(currentUser.uid, likedUserId),
                    "timestamp" to Date(),
                    "lastMessage" to "",
                    "lastMessageTime" to null,
                    "isActive" to true
                )

                firestore.collection("matches")
                    .add(matchData)
                    .await()

                // Recarregar matches
                loadMatches()
                return true
            }

            return false
        } catch (e: Exception) {
            return false
        }
    }
}
