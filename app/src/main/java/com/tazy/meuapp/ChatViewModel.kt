package com.tazy.meuapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var mensagensListener: ListenerRegistration? = null

    // Nome do usuário logado, obtido do Firestore
    private var nomeUsuarioLogado: String = "Anônimo"

    init {
        // Carrega o nome do usuário apenas uma vez
        viewModelScope.launch {
            auth.currentUser?.uid?.let { uid ->
                try {
                    val doc = firestore.collection("usuarios").document(uid).get().await()
                    nomeUsuarioLogado = doc.getString("nome") ?: "Anônimo"
                } catch (_: Exception) {
                    nomeUsuarioLogado = "Anônimo"
                }
            }
        }
    }

    fun iniciar(grupoId: String) {
        _state.value = _state.value.copy(nomeGrupo = "")
        ouvirMensagens(grupoId)
        carregarDadosGrupo(grupoId) { nome ->
            _state.value = _state.value.copy(nomeGrupo = nome)
        }
    }

    fun carregarDadosGrupo(grupoId: String, onNomeCarregado: (String) -> Unit) = viewModelScope.launch {
        try {
            val document = firestore.collection("grupos").document(grupoId).get().await()
            val nome = document.getString("nome") ?: "Grupo"
            onNomeCarregado(nome)
        } catch (e: Exception) {
            onNomeCarregado("Grupo")
        }
    }

    private fun ouvirMensagens(grupoId: String) {
        mensagensListener?.remove()
        mensagensListener = firestore.collection("grupos")
            .document(grupoId)
            .collection("mensagens")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _state.value = _state.value.copy(erro = error.message)
                    return@addSnapshotListener
                }

                val mensagens = snapshot?.documents?.mapNotNull { doc ->
                    Mensagem(
                        id = doc.id,
                        texto = doc.getString("texto") ?: "",
                        remetenteId = doc.getString("remetenteId") ?: "",
                        remetenteNome = doc.getString("remetenteNome") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                } ?: emptyList()

                _state.value = _state.value.copy(mensagens = mensagens)
            }
    }

    fun enviarMensagem(grupoId: String, texto: String) = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch

        // Garante que nomeUsuarioLogado esteja carregado
        if (nomeUsuarioLogado.isBlank()) {
            try {
                val doc = firestore.collection("usuarios").document(uid).get().await()
                nomeUsuarioLogado = doc.getString("nome") ?: "Anônimo"
            } catch (_: Exception) {
                nomeUsuarioLogado = "Anônimo"
            }
        }

        val novaMensagem = mapOf(
            "texto" to texto,
            "remetenteId" to uid,
            "remetenteNome" to nomeUsuarioLogado,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("grupos")
            .document(grupoId)
            .collection("mensagens")
            .add(novaMensagem)
            .addOnFailureListener {
                _state.value = _state.value.copy(erro = "Erro ao enviar mensagem")
            }
    }

    override fun onCleared() {
        super.onCleared()
        mensagensListener?.remove()
    }
}
