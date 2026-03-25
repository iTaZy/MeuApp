package com.tazy.meuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tazy.meuapp.model.Match
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

sealed class MatchesUiState {
    object Loading : MatchesUiState()
    data class Success(val matches: List<Match>, val arquivadas: List<Match> = emptyList()) : MatchesUiState()
    data class Error(val message: String) : MatchesUiState()
    object Empty : MatchesUiState()
}

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
                        val matchesGerais = mutableListOf<Match>()

                        for (document in snapshot.documents) {
                            val participants = document.get("participants") as? List<String> ?: continue
                            val user1Id = participants.getOrNull(0) ?: continue
                            val user2Id = participants.getOrNull(1) ?: continue

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
                                isLastMessageRead = document.getBoolean("isLastMessageRead") ?: true,
                                arquivadosPor = document.get("arquivadosPor") as? List<String> ?: emptyList(),
                                nicknames = document.get("nicknames") as? Map<String, String> ?: emptyMap()
                            )
                            matchesGerais.add(match)
                        }

                        val ativas = matchesGerais.filter { !it.arquivadosPor.contains(currentUser.uid) }
                        val arquivadas = matchesGerais.filter { it.arquivadosPor.contains(currentUser.uid) }

                        if (ativas.isNotEmpty() || arquivadas.isNotEmpty()) {
                            _uiState.value = MatchesUiState.Success(ativas, arquivadas)
                        } else {
                            _uiState.value = MatchesUiState.Empty
                        }

                    } catch (e: Exception) {
                        _uiState.value = MatchesUiState.Error("Erro ao processar matches.")
                    }
                }
            }
    }

    suspend fun createMatchIfExists(likedUserId: String): Boolean {
        val currentUser = auth.currentUser ?: return false
        try {
            val mutualLikeQuery = firestore.collection("conexoes")
                .whereEqualTo("userId", likedUserId)
                .whereEqualTo("likedUserId", currentUser.uid)
                .whereEqualTo("liked", true)
                .get()
                .await()

            if (mutualLikeQuery.documents.isNotEmpty()) {
                val matchData = hashMapOf(
                    "participants" to listOf(currentUser.uid, likedUserId),
                    "timestamp" to Date(),
                    "lastMessage" to "",
                    "lastMessageTime" to null,
                    "isActive" to true
                )
                firestore.collection("matches").add(matchData).await()
                loadMatches()
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }
}