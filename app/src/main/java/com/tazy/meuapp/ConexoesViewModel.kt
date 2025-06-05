// ConexoesViewModel.kt (Versão atualizada com integração de matches)
package com.tazy.meuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tazy.meuapp.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class ConexoesViewModel : ViewModel() {
    // Estado da tela
    private val _uiState = MutableStateFlow<ConexoesUiState>(ConexoesUiState.Loading)
    val uiState: StateFlow<ConexoesUiState> = _uiState.asStateFlow()

    // Estado para notificar match
    private val _matchState = MutableStateFlow<MatchState>(MatchState.None)
    val matchState: StateFlow<MatchState> = _matchState.asStateFlow()

    // Lista de perfis disponíveis
    private val availableProfiles = mutableListOf<Profile>()

    // Índice do perfil atual
    private var currentProfileIndex = 0

    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            try {
                _uiState.value = ConexoesUiState.Loading

                val currentUser = auth.currentUser

                if (currentUser == null) {
                    _uiState.value = ConexoesUiState.Error("Usuário não autenticado")
                    return@launch
                }

                // Primeiro, obter o código do condomínio do usuário atual
                val currentUserDoc = firestore.collection("usuarios")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val codigoCondominio = currentUserDoc.getString("codigoCondominio")
                if (codigoCondominio.isNullOrBlank()) {
                    _uiState.value = ConexoesUiState.Error("Código do condomínio não encontrado")
                    return@launch
                }

                // Buscar outros usuários do mesmo condomínio (excluindo o usuário atual)
                val querySnapshot = firestore.collection("usuarios")
                    .whereEqualTo("codigoCondominio", codigoCondominio)
                    .get()
                    .await()

                // Buscar perfis já avaliados pelo usuário atual
                val avaliadosSnapshot = firestore.collection("conexoes")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                val perfilsAvaliados = avaliadosSnapshot.documents.map {
                    it.getString("likedUserId")
                }.toSet()

                val profiles = mutableListOf<Profile>()

                for (document in querySnapshot.documents) {
                    // Pular o próprio usuário
                    if (document.id == currentUser.uid) continue

                    // Pular perfis já avaliados
                    if (perfilsAvaliados.contains(document.id)) continue

                    val nome = document.getString("nome") ?: continue
                    val idade = document.getLong("idade")?.toInt() ?: continue
                    val bio = document.getString("bio") ?: ""
                    val sexualidade = document.getString("sexualidade") ?: ""
                    val signo = document.getString("signo") ?: ""
                    val interesses = document.get("interesses") as? List<String> ?: emptyList()

                    val profile = Profile(
                        id = document.id,
                        name = nome,
                        imageUrl = "", // Por enquanto vazio, pode ser implementado depois
                        age = idade,
                        bio = if (bio.isNotEmpty()) bio else "Sem bio disponível",
                        sexualidade = sexualidade,
                        signo = signo,
                        interesses = interesses
                    )

                    profiles.add(profile)
                }

                availableProfiles.clear()
                availableProfiles.addAll(profiles)

                if (profiles.isNotEmpty()) {
                    _uiState.value = ConexoesUiState.Success(
                        currentProfile = profiles.first(),
                        remainingProfiles = profiles.size - 1,
                        currentIndex = 0,
                        totalProfiles = profiles.size
                    )
                } else {
                    _uiState.value = ConexoesUiState.Empty
                }
            } catch (e: Exception) {
                _uiState.value = ConexoesUiState.Error(
                    message = "Erro ao carregar perfis: ${e.localizedMessage ?: "Erro desconhecido"}"
                )
            }
        }
    }

    fun likeProfile() {
        val currentProfile = availableProfiles.getOrNull(currentProfileIndex) ?: return
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                // Salvar o like no Firestore
                val conexaoData = hashMapOf(
                    "userId" to currentUser.uid,
                    "likedUserId" to currentProfile.id,
                    "liked" to true,
                    "timestamp" to Date()
                )

                firestore.collection("conexoes")
                    .add(conexaoData)
                    .await()

                // Verificar se existe match mútuo
                val mutualLikeQuery = firestore.collection("conexoes")
                    .whereEqualTo("userId", currentProfile.id)
                    .whereEqualTo("likedUserId", currentUser.uid)
                    .whereEqualTo("liked", true)
                    .get()
                    .await()

                if (mutualLikeQuery.documents.isNotEmpty()) {
                    // MATCH! Criar documento de match
                    val matchData = hashMapOf(
                        "participants" to listOf(currentUser.uid, currentProfile.id),
                        "timestamp" to Date(),
                        "lastMessage" to "",
                        "lastMessageTime" to null,
                        "isActive" to true
                    )

                    firestore.collection("matches")
                        .add(matchData)
                        .await()

                    // Notificar sobre o match
                    _matchState.value = MatchState.NewMatch(currentProfile.name)

                    // Limpar notificação após 3 segundos
                    delay(3000)
                    _matchState.value = MatchState.None
                }

                // Simula um pequeno delay para feedback visual
                delay(200)

                // Avança para o próximo perfil
                moveToNextProfile()

            } catch (e: Exception) {
                // Em caso de erro, ainda avança (pode implementar tratamento de erro aqui)
                moveToNextProfile()
            }
        }
    }

    // Função para "dislike" (pular perfil sem criar vínculo)
    fun dislikeProfile() {
        val currentProfile = availableProfiles.getOrNull(currentProfileIndex) ?: return
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                // Salvar o dislike no Firestore para não mostrar novamente
                val conexaoData = hashMapOf(
                    "userId" to currentUser.uid,
                    "likedUserId" to currentProfile.id,
                    "liked" to false,
                    "timestamp" to Date()
                )

                firestore.collection("conexoes")
                    .add(conexaoData)
                    .await()

                // Simula um pequeno delay para feedback visual
                delay(200)

                // Avança para o próximo perfil
                moveToNextProfile()

            } catch (e: Exception) {
                // Em caso de erro, ainda avança
                moveToNextProfile()
            }
        }
    }

    // Nova função para navegar para o próximo perfil
    fun nextProfile() {
        if (currentProfileIndex < availableProfiles.size - 1) {
            currentProfileIndex++
            updateUiState()
        }
    }

    // Nova função para navegar para o perfil anterior
    fun previousProfile() {
        if (currentProfileIndex > 0) {
            currentProfileIndex--
            updateUiState()
        }
    }

    // Função auxiliar para atualizar o estado da UI
    private fun updateUiState() {
        if (currentProfileIndex < availableProfiles.size) {
            _uiState.value = ConexoesUiState.Success(
                currentProfile = availableProfiles[currentProfileIndex],
                remainingProfiles = availableProfiles.size - currentProfileIndex - 1,
                currentIndex = currentProfileIndex,
                totalProfiles = availableProfiles.size
            )
        } else {
            _uiState.value = ConexoesUiState.Empty
        }
    }

    private fun moveToNextProfile() {
        currentProfileIndex++
        if (currentProfileIndex < availableProfiles.size) {
            _uiState.value = ConexoesUiState.Success(
                currentProfile = availableProfiles[currentProfileIndex],
                remainingProfiles = availableProfiles.size - currentProfileIndex - 1,
                currentIndex = currentProfileIndex,
                totalProfiles = availableProfiles.size
            )
        } else {
            _uiState.value = ConexoesUiState.Empty
        }
    }

    // Função para verificar se pode voltar
    fun canGoBack(): Boolean = currentProfileIndex > 0

    // Função para verificar se pode avançar
    fun canGoForward(): Boolean = currentProfileIndex < availableProfiles.size - 1

    // Função para recarregar perfis (útil para pull-to-refresh)
    fun reloadProfiles() {
        currentProfileIndex = 0
        availableProfiles.clear()
        _uiState.value = ConexoesUiState.Loading
        loadProfiles()
    }

    // Função para limpar estado de match
    fun clearMatchState() {
        _matchState.value = MatchState.None
    }
}

// Estados possíveis da UI
sealed class ConexoesUiState {
    object Loading : ConexoesUiState()
    data class Success(
        val currentProfile: Profile,
        val remainingProfiles: Int,
        val currentIndex: Int,
        val totalProfiles: Int
    ) : ConexoesUiState()
    data class Error(val message: String) : ConexoesUiState()
    object Empty : ConexoesUiState()
}

// Estados para matches
sealed class MatchState {
    object None : MatchState()
    data class NewMatch(val matchedUserName: String) : MatchState()
}