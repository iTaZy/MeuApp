package com.tazy.meuapp

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TelaPrincipalViewModel : ViewModel() {

    private val _state = MutableStateFlow(TelaPrincipalState())
    val state: StateFlow<TelaPrincipalState> = _state

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var usuarioListener: ListenerRegistration? = null
    private var gruposListener: ListenerRegistration? = null

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        observarUsuario()
        carregarGruposAtivos()
        carregarGruposRecomendados()
    }

    fun atualizarDados() {
        carregarGruposAtivos()
        carregarGruposRecomendados()
    }

    private fun observarUsuario() {
        val uid = auth.currentUser?.uid ?: run {
            _state.value = _state.value.copy(erro = "Usuário não autenticado")
            return
        }

        usuarioListener = firestore.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _state.value = _state.value.copy(erro = error.message)
                    return@addSnapshotListener
                }
                snapshot?.getString("nome")?.let { nome ->
                    _state.value = _state.value.copy(nomeUsuario = nome)
                }
            }
    }

    private fun carregarGruposAtivos() {
        val uid = auth.currentUser?.uid ?: return

        gruposListener?.remove()
        gruposListener = firestore.collection("grupos")
            .whereArrayContains("participantes", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _state.value = _state.value.copy(erro = error.message)
                    return@addSnapshotListener
                }

                val chats = snapshot?.documents?.mapNotNull { doc ->
                    Chat(
                        id = doc.id,
                        nome = doc.getString("nome") ?: return@mapNotNull null,
                        ultimaMensagem = doc.getString("ultimaMensagem") ?: "",
                        mensagensNaoLidas = (doc.getLong("mensagensNaoLidas") ?: 0).toInt()
                    )
                }?.distinctBy { it.id } ?: emptyList()

                _state.value = _state.value.copy(chatsAtivos = chats)
            }
    }

    fun carregarGruposRecomendados() = viewModelScope.launch {
        _state.value = _state.value.copy(carregando = true)
        try {
            val userId = auth.currentUser?.uid ?: run {
                _state.value = _state.value.copy(erro = "Usuário não autenticado", carregando = false)
                return@launch
            }

            val usuarioDoc = firestore.collection("usuarios").document(userId).get().await()
            val gruposUsuario = usuarioDoc.get("grupos") as? List<String> ?: emptyList()
            val interesses = usuarioDoc.get("interesses") as? List<String> ?: emptyList()

            // Consulta aprimorada usando múltiplos interesses
            val baseQuery = if (interesses.isNotEmpty()) {
                firestore.collection("grupos")
                    .whereArrayContainsAny("interesses", interesses.take(10))
                    .limit(10)
            } else {
                firestore.collection("grupos")
                    .orderBy("membrosCount", Query.Direction.DESCENDING)
                    .limit(10)
            }

            val grupos = baseQuery.get().await().documents
                .filter { doc -> !gruposUsuario.contains(doc.id) }
                .mapNotNull { doc ->
                    GrupoRecomendado(
                        id = doc.id,
                        nome = doc.getString("nome") ?: return@mapNotNull null,
                        descricao = doc.getString("descricao") ?: "Grupo sem descrição",
                        relevancia = (doc.getLong("relevancia") ?: 3).toInt(),
                        membrosCount = (doc.getLong("membrosCount") ?: 1).toInt()
                    )
                }

            _state.value = _state.value.copy(
                gruposRecomendados = grupos,
                carregando = false,
                erro = null
            )

        } catch (e: Exception) {
            _state.value = _state.value.copy(
                erro = "Erro ao carregar grupos: ${e.message}",
                carregando = false
            )
        }
    }

    fun entrarNoGrupo(grupoId: String) = viewModelScope.launch {
        _state.value = _state.value.copy(carregando = true)
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("Usuário não autenticado")

            // Feedback imediato
            _state.value = _state.value.copy(
                gruposRecomendados = _state.value.gruposRecomendados.filter { it.id != grupoId }
            )

            val batch = firestore.batch()
            val grupoRef = firestore.collection("grupos").document(grupoId)
            batch.update(grupoRef,
                "participantes", FieldValue.arrayUnion(userId),
                "membrosCount", FieldValue.increment(1)
            )
            val usuarioRef = firestore.collection("usuarios").document(userId)
            batch.update(usuarioRef,
                "grupos", FieldValue.arrayUnion(grupoId)
            )
            batch.commit().await()

            // Atualiza recomendações após entrar
            carregarGruposRecomendados()
            _state.value = _state.value.copy(carregando = false)

        } catch (e: Exception) {
            _state.value = _state.value.copy(
                erro = "Erro ao entrar no grupo: ${e.message}",
                carregando = false
            )
        }
    }

    fun sairDoGrupo(grupoId: String) = viewModelScope.launch {
        _state.value = _state.value.copy(carregando = true)
        try {
            val userId = auth.currentUser?.uid ?: return@launch

            val batch = firestore.batch()
            val grupoRef = firestore.collection("grupos").document(grupoId)
            batch.update(grupoRef,
                "participantes", FieldValue.arrayRemove(userId),
                "membrosCount", FieldValue.increment(-1)
            )
            val usuarioRef = firestore.collection("usuarios").document(userId)
            batch.update(usuarioRef,
                "grupos", FieldValue.arrayRemove(grupoId)
            )
            batch.commit().await()

            // Recarrega recomendações após sair
            carregarGruposRecomendados()
            _state.value = _state.value.copy(carregando = false)

        } catch (e: Exception) {
            _state.value = _state.value.copy(
                erro = "Erro ao sair do grupo: ${e.message}",
                carregando = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        usuarioListener?.remove()
        gruposListener?.remove()
    }
}
