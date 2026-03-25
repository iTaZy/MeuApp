package com.tazy.meuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class PerfilUiState {
    object Loading : PerfilUiState()
    data class Success(
        val nome: String,
        val idade: Int,
        val bio: String,
        val sexualidade: String,
        val signo: String,
        val interessePrincipal: String,
        val subcategorias: List<String>,
        val outrosInteresses: List<String>
    ) : PerfilUiState()
    data class Error(val message: String) : PerfilUiState()
}

@HiltViewModel
class PerfilUsuarioViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Loading)
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun carregarPerfil(userId: String) {
        viewModelScope.launch {
            _uiState.value = PerfilUiState.Loading
            try {
                val document = firestore.collection("usuarios").document(userId).get().await()

                if (document.exists()) {
                    val nome = document.getString("nome") ?: "Usuário"
                    val idade = document.getLong("idade")?.toInt() ?: 0
                    val bio = document.getString("bio") ?: ""
                    val sexualidade = document.getString("sexualidade") ?: ""
                    val signo = document.getString("signo") ?: ""
                    val interessePrincipal = document.getString("interesse") ?: ""
                    val subcategorias = document.get("subcategorias") as? List<String> ?: emptyList()
                    val outrosInteresses = document.get("outrosInteresses") as? List<String> ?: emptyList()

                    _uiState.value = PerfilUiState.Success(
                        nome = nome,
                        idade = idade,
                        bio = bio,
                        sexualidade = sexualidade,
                        signo = signo,
                        interessePrincipal = interessePrincipal,
                        subcategorias = subcategorias,
                        outrosInteresses = outrosInteresses
                    )
                } else {
                    _uiState.value = PerfilUiState.Error("Usuário não encontrado.")
                }
            } catch (e: Exception) {
                _uiState.value = PerfilUiState.Error("Erro ao carregar o perfil.")
            }
        }
    }
}