// TelaPrincipalViewModel.kt
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

        val usuarioDoc = firestore.collection("usuarios").document(uid).get().await()
        val nome = usuarioDoc.getString("nome") ?: ""
        val meusGrupos = usuarioDoc.get("grupos") as? List<String> ?: emptyList()
        val interesses = usuarioDoc.get("interesses") as? List<String> ?: emptyList()
        val meuCodigo = usuarioDoc.getString("codigoCondominio") ?: ""

        _state.value = _state.value.copy(
            nomeUsuario = nome,
            codigoCondominio = meuCodigo
        )

        observarUsuario()
        carregarGruposAtivos(meuCodigo, uid)
        carregarGruposRecomendados(meuCodigo, meusGrupos, interesses)

        _state.value = _state.value.copy(carregando = false)
    }

    fun atualizarDados() {
        val current = _state.value
        viewModelScope.launch {
            _state.value = current.copy(carregando = true)
            val uid = auth.currentUser?.uid.orEmpty()
            val usuarioDoc = firestore.collection("usuarios").document(uid).get().await()
            carregarGruposAtivos(current.codigoCondominio, uid)
            carregarGruposRecomendados(
                current.codigoCondominio,
                usuarioDoc.get("grupos") as? List<String> ?: emptyList(),
                usuarioDoc.get("interesses") as? List<String> ?: emptyList()
            )
            _state.value = _state.value.copy(carregando = false)
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

    private fun carregarGruposAtivos(codigoCondominio: String, uid: String) {
        gruposListener?.remove()
        gruposListener = firestore.collection("grupos")
            .whereEqualTo("codigoCondominio", codigoCondominio)
            .whereArrayContains("participantes", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _state.value = _state.value.copy(erro = error.message)
                    return@addSnapshotListener
                }
                val chats = snapshot?.documents
                    ?.mapNotNull { doc ->
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

    private fun carregarGruposRecomendados(
        codigoCondominio: String,
        gruposUsuario: List<String>,
        interesses: List<String>
    ) = viewModelScope.launch {
        _state.value = _state.value.copy(carregando = true)
        try {
            println("Buscando grupos com código: $codigoCondominio e interesses: $interesses")

            val docs = if (interesses.isNotEmpty()) {
                firestore.collection("grupos")
                    .whereEqualTo("codigoCondominio", codigoCondominio)
                    .whereArrayContainsAny("interesses", interesses.take(10))
                    .get()
                    .await()
            } else {
                firestore.collection("grupos")
                    .whereEqualTo("codigoCondominio", codigoCondominio)
                    .orderBy("membrosCount", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .await()
            }

            val grupos = docs.documents
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

            println("Encontrados ${grupos.size} grupos recomendados")

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

            val usuarioDoc = firestore.collection("usuarios").document(uid).get().await()
            carregarGruposRecomendados(
                _state.value.codigoCondominio,
                usuarioDoc.get("grupos") as? List<String> ?: emptyList(),
                usuarioDoc.get("interesses") as? List<String> ?: emptyList()
            )
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

            val usuarioDoc = firestore.collection("usuarios").document(uid).get().await()
            carregarGruposRecomendados(
                _state.value.codigoCondominio,
                usuarioDoc.get("grupos") as? List<String> ?: emptyList(),
                usuarioDoc.get("interesses") as? List<String> ?: emptyList()
            )
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
