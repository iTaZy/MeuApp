// ChatViewModel.kt
package com.tazy.meuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import com.tazy.meuapp.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

// Estados para o Chat
sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(
        val messages: List<ChatMessage>,
        val otherUserName: String,
        val currentUserId: String
    ) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class ChatIndividualViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var messagesListener: ListenerRegistration? = null
    private var currentMatchId: String? = null
    private var otherUserName: String = ""

    fun loadMessages(matchId: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = ChatUiState.Error("Usuário não autenticado")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = ChatUiState.Loading
                currentMatchId = matchId

                // Primeiro, buscar informações do match para obter o nome do outro usuário
                val matchDoc = firestore.collection("matches")
                    .document(matchId)
                    .get()
                    .await()

                if (!matchDoc.exists()) {
                    _uiState.value = ChatUiState.Error("Match não encontrado")
                    return@launch
                }

                val participants = matchDoc.get("participants") as? List<String> ?: emptyList()
                val otherUserId = participants.firstOrNull { it != currentUser.uid }

                if (otherUserId == null) {
                    _uiState.value = ChatUiState.Error("Erro ao encontrar outro usuário")
                    return@launch
                }

                // Buscar nome do outro usuário
                val otherUserDoc = firestore.collection("usuarios")
                    .document(otherUserId)
                    .get()
                    .await()

                otherUserName = otherUserDoc.getString("nome") ?: "Usuário"

                // Configurar listener em tempo real para as mensagens
                setupMessagesListener(matchId, currentUser.uid)

            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error("Erro ao carregar chat: ${e.localizedMessage}")
            }
        }
    }

    private fun setupMessagesListener(matchId: String, currentUserId: String) {
        messagesListener?.remove()

        messagesListener = firestore.collection("matches")
            .document(matchId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = ChatUiState.Error("Erro ao escutar mensagens: ${error.localizedMessage}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.documents.map { document ->
                        ChatMessage(
                            id = document.id,
                            matchId = matchId,
                            senderId = document.getString("senderId") ?: "",
                            senderName = document.getString("senderName") ?: "",
                            message = document.getString("message") ?: "",
                            timestamp = document.getDate("timestamp") ?: Date(),
                            isRead = document.getBoolean("isRead") ?: false
                        )
                    }
                    _uiState.value = ChatUiState.Success(
                        messages = messages,
                        otherUserName = otherUserName,
                        currentUserId = currentUserId
                    )
                }
            }
    }



    fun sendMessage(matchId: String, messageText: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                // Buscar o nome do usuário atual
                val currentUserDoc = firestore.collection("usuarios")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val currentUserName = currentUserDoc.getString("nome") ?: "Usuário"

                // Criar dados da nova mensagem
                val messageData = hashMapOf(
                    "matchId" to matchId,
                    "senderId" to currentUser.uid,
                    "senderName" to currentUserName,
                    "message" to messageText,
                    "timestamp" to Date(),
                    "isRead" to false
                )

                // Adicionar a mensagem na subcoleção do match
                val messageRef = firestore.collection("matches")
                    .document(matchId)
                    .collection("messages")
                    .add(messageData)
                    .await()

                // Log para debug
                println("Mensagem enviada com sucesso. ID: ${messageRef.id}")

                // Atualizar o documento do match com a última mensagem
                val matchUpdateData = mapOf(
                    "lastMessage" to messageText,
                    "lastMessageTime" to Date()
                )

                firestore.collection("matches")
                    .document(matchId)
                    .update(matchUpdateData)
                    .await()

                println("Match atualizado com última mensagem")

            } catch (e: Exception) {
                println("Erro ao enviar mensagem: ${e.localizedMessage}")
            }
        }
    }

    fun markMessagesAsRead(matchId: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                // Marcar como lidas todas as mensagens do match que não são do usuário atual
                val unreadMessages = firestore.collection("matches")
                    .document(matchId)
                    .collection("messages")
                    .whereNotEqualTo("senderId", currentUser.uid)
                    .whereEqualTo("isRead", false)
                    .get()
                    .await()

                for (document in unreadMessages.documents) {
                    document.reference.update("isRead", true)
                }
            } catch (e: Exception) {
                // Falha silenciosa
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
    }
}