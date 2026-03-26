package com.tazy.meuapp

import android.util.Log
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
        inicializar()
    }

    private fun inicializar() = viewModelScope.launch {
        _state.value = _state.value.copy(carregando = true)

        val uid = auth.currentUser?.uid ?: run {
            _state.value = _state.value.copy(erro = "Usuário não autenticado", carregando = false)
            return@launch
        }

        try {
            val usuarioDoc = firestore.collection("usuarios").document(uid).get().await()
            val nome = usuarioDoc.getString("nome") ?: ""
            val meusGrupos = usuarioDoc.get("grupos") as? List<String> ?: emptyList()

            val interessePrincipal = usuarioDoc.getString("interesse") ?: ""
            val subcategorias = usuarioDoc.get("subcategorias") as? List<String> ?: emptyList()
            val outrosInteresses = usuarioDoc.get("outrosInteresses") as? List<String> ?: emptyList()

            val todosInteresses = mutableSetOf<String>()
            if (interessePrincipal.isNotBlank()) todosInteresses.add(interessePrincipal)
            todosInteresses.addAll(subcategorias.filter { it.isNotBlank() })
            todosInteresses.addAll(outrosInteresses.filter { it.isNotBlank() })

            _state.value = _state.value.copy(nomeUsuario = nome)

            observarUsuario()
            carregarGruposAtivos(uid)
            carregarGruposRecomendados(meusGrupos, todosInteresses.toList())

        } catch (e: Exception) {
            _state.value = _state.value.copy(erro = "Erro ao carregar dados: ${e.message}", carregando = false)
        }
    }

    fun atualizarDados() {
        val current = _state.value
        viewModelScope.launch {
            _state.value = current.copy(carregando = true)
            val uid = auth.currentUser?.uid ?: return@launch

            try {
                val usuarioDoc = firestore.collection("usuarios").document(uid).get().await()

                val interessePrincipal = usuarioDoc.getString("interesse") ?: ""
                val subcategorias = usuarioDoc.get("subcategorias") as? List<String> ?: emptyList()
                val outrosInteresses = usuarioDoc.get("outrosInteresses") as? List<String> ?: emptyList()

                val todosInteresses = mutableSetOf<String>()
                if (interessePrincipal.isNotBlank()) todosInteresses.add(interessePrincipal)
                todosInteresses.addAll(subcategorias.filter { it.isNotBlank() })
                todosInteresses.addAll(outrosInteresses.filter { it.isNotBlank() })

                carregarGruposAtivos(uid)
                carregarGruposRecomendados(
                    usuarioDoc.get("grupos") as? List<String> ?: emptyList(),
                    todosInteresses.toList()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(erro = "Erro ao atualizar: ${e.message}", carregando = false)
            }
        }
    }

    private fun observarUsuario() {
        val uid = auth.currentUser?.uid ?: return
        usuarioListener = firestore.collection("usuarios").document(uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    _state.value = _state.value.copy(erro = err.message)
                    return@addSnapshotListener
                }
                snap?.getString("nome")?.let { nome ->
                    _state.value = _state.value.copy(nomeUsuario = nome)
                }
            }
    }

    private fun carregarGruposAtivos(uid: String) {
        gruposListener?.remove()
        gruposListener = firestore.collection("grupos")
            .whereArrayContains("participantes", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _state.value = _state.value.copy(erro = error.message)
                    return@addSnapshotListener
                }
                val chats = snapshot?.documents
                    ?.mapNotNull { doc ->
                        val naoLidasMap = doc.get("naoLidas") as? Map<String, Number>
                        val minhasNaoLidas = naoLidasMap?.get(uid)?.toInt() ?: 0

                        Chat(
                            id = doc.id,
                            nome = doc.getString("nome") ?: return@mapNotNull null,
                            ultimaMensagem = doc.getString("ultimaMensagem") ?: "",
                            mensagensNaoLidas = minhasNaoLidas
                        )
                    }?.distinctBy { it.id } ?: emptyList()

                _state.value = _state.value.copy(chatsAtivos = chats, carregando = false)
            }
    }

    private fun carregarGruposRecomendados(
        gruposUsuario: List<String>,
        interesses: List<String>
    ) = viewModelScope.launch {
        _state.value = _state.value.copy(carregando = true)
        try {
            val gruposRecomendados = mutableListOf<GrupoRecomendado>()

            if (interesses.isNotEmpty()) {
                val interessesParaBusca = interesses.take(10)
                val docsInteresses = firestore.collection("grupos")
                    .whereArrayContainsAny("interesses", interessesParaBusca)
                    .limit(20)
                    .get()
                    .await()

                val gruposPorInteresse = docsInteresses.documents
                    .filter { !gruposUsuario.contains(it.id) }
                    .mapNotNull { doc ->
                        GrupoRecomendado(
                            id = doc.id,
                            nome = doc.getString("nome") ?: return@mapNotNull null,
                            descricao = doc.getString("descricao") ?: "Grupo sem descrição",
                            relevancia = (doc.getLong("relevancia") ?: 3).toInt(),
                            membrosCount = (doc.getLong("membrosCount") ?: 1).toInt(),
                            interesses = doc.get("interesses") as? List<String> ?: emptyList() // AQUI!
                        )
                    }
                gruposRecomendados.addAll(gruposPorInteresse)
            }

            if (gruposRecomendados.size < 5) {
                val docsPopulares = firestore.collection("grupos")
                    .orderBy("membrosCount", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .await()

                val gruposPopulares = docsPopulares.documents
                    .filter { !gruposUsuario.contains(it.id) && gruposRecomendados.none { g -> g.id == it.id } }
                    .mapNotNull { doc ->
                        GrupoRecomendado(
                            id = doc.id,
                            nome = doc.getString("nome") ?: return@mapNotNull null,
                            descricao = doc.getString("descricao") ?: "Grupo sem descrição",
                            relevancia = (doc.getLong("relevancia") ?: 3).toInt(),
                            membrosCount = (doc.getLong("membrosCount") ?: 1).toInt(),
                            interesses = doc.get("interesses") as? List<String> ?: emptyList() // E AQUI!
                        )
                    }
                gruposRecomendados.addAll(gruposPopulares)
            }

            _state.value = _state.value.copy(
                gruposRecomendados = gruposRecomendados.distinctBy { it.id },
                carregando = false,
                erro = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                erro = "Erro ao carregar recomendações: ${e.message}",
                carregando = false
            )
        }
    }

    fun entrarNoGrupo(grupoId: String) = viewModelScope.launch {
        _state.value = _state.value.copy(carregando = true)
        try {
            val uid = auth.currentUser?.uid ?: throw Exception("Usuário não autenticado")

            _state.value = _state.value.copy(
                gruposRecomendados = _state.value.gruposRecomendados.filter { it.id != grupoId }
            )

            val batch = firestore.batch()
            val gRef = firestore.collection("grupos").document(grupoId)
            batch.update(gRef,
                "participantes", FieldValue.arrayUnion(uid),
                "membrosCount", FieldValue.increment(1)
            )
            val uRef = firestore.collection("usuarios").document(uid)
            batch.update(uRef,
                "grupos", FieldValue.arrayUnion(grupoId)
            )
            batch.commit().await()

            atualizarDados()
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
            val uid = auth.currentUser?.uid ?: return@launch

            val batch = firestore.batch()
            val gRef = firestore.collection("grupos").document(grupoId)
            batch.update(gRef,
                "participantes", FieldValue.arrayRemove(uid),
                "membrosCount", FieldValue.increment(-1)
            )
            val uRef = firestore.collection("usuarios").document(uid)
            batch.update(uRef,
                "grupos", FieldValue.arrayRemove(grupoId)
            )
            batch.commit().await()

            atualizarDados()
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