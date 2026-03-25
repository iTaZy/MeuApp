package com.tazy.meuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(
        val messages: List<ChatMessage>,
        val otherUserName: String,
        val currentUserId: String,
        val isArchived: Boolean,
        val otherUserId: String // NOVO: Agora o Chat entrega o ID da outra pessoa para a tela
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
    private var isChatArchived: Boolean = false
    private var currentOtherUserId: String = "" // Guardamos o ID aqui

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

                val matchDoc = firestore.collection("matches").document(matchId).get().await()

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

                currentOtherUserId = otherUserId // Salva o ID da outra pessoa

                val arquivadosPor = matchDoc.get("arquivadosPor") as? List<String> ?: emptyList()
                isChatArchived = arquivadosPor.contains(currentUser.uid)

                val nicknames = matchDoc.get("nicknames") as? Map<String, String> ?: emptyMap()
                val otherUserDoc = firestore.collection("usuarios").document(otherUserId).get().await()
                val originalName = otherUserDoc.getString("nome") ?: "Usuário"

                otherUserName = nicknames[currentUser.uid] ?: originalName

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
                        currentUserId = currentUserId,
                        isArchived = isChatArchived,
                        otherUserId = currentOtherUserId // Passa o ID para a UI
                    )
                }
            }
    }

    fun sendMessage(matchId: String, messageText: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val currentUserDoc = firestore.collection("usuarios").document(currentUser.uid).get().await()
                val currentUserName = currentUserDoc.getString("nome") ?: "Usuário"

                val messageData = hashMapOf(
                    "matchId" to matchId,
                    "senderId" to currentUser.uid,
                    "senderName" to currentUserName,
                    "message" to messageText,
                    "timestamp" to Date(),
                    "isRead" to false
                )
                firestore.collection("matches").document(matchId).collection("messages").add(messageData).await()

                val matchUpdateData = mapOf(
                    "lastMessage" to messageText,
                    "lastMessageTime" to Date(),
                    "lastMessageSenderId" to currentUser.uid,
                    "isLastMessageRead" to false
                )
                firestore.collection("matches").document(matchId).update(matchUpdateData).await()
            } catch (e: Exception) {
                println("Erro ao enviar mensagem: ${e.localizedMessage}")
            }
        }
    }

    fun markMessagesAsRead(matchId: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
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
                firestore.collection("matches").document(matchId).update("isLastMessageRead", true)
            } catch (e: Exception) { }
        }
    }

    fun arquivarConversa(matchId: String, onSucesso: () -> Unit) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                firestore.collection("matches").document(matchId)
                    .update("arquivadosPor", FieldValue.arrayUnion(currentUser.uid))
                    .await()
                onSucesso()
            } catch (e: Exception) { }
        }
    }

    fun desarquivarConversa(matchId: String, onSucesso: () -> Unit) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                firestore.collection("matches").document(matchId)
                    .update("arquivadosPor", FieldValue.arrayRemove(currentUser.uid))
                    .await()
                onSucesso()
            } catch (e: Exception) { }
        }
    }

    fun salvarApelido(matchId: String, novoApelido: String, onSucesso: () -> Unit) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                firestore.collection("matches").document(matchId)
                    .update("nicknames.${currentUser.uid}", novoApelido)
                    .await()

                otherUserName = novoApelido
                val currentState = _uiState.value
                if (currentState is ChatUiState.Success) {
                    _uiState.value = currentState.copy(otherUserName = novoApelido)
                }
                onSucesso()
            } catch (e: Exception) { }
        }
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
    }
}