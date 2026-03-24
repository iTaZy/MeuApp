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
    private val _uiState = MutableStateFlow<ConexoesUiState>(ConexoesUiState.Loading)
    val uiState: StateFlow<ConexoesUiState> = _uiState.asStateFlow()

    private val _matchState = MutableStateFlow<MatchState>(MatchState.None)
    val matchState: StateFlow<MatchState> = _matchState.asStateFlow()

    private val availableProfiles = mutableListOf<Profile>()
    private var currentProfileIndex = 0

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // --- VARIÁVEIS DOS FILTROS ---
    var filtroIdadeMinima = 18
        private set
    var filtroIdadeMaxima = 100
        private set
    var filtroInteresseFocado: String = ""
        private set
    var filtroSexualidade: String = ""
        private set

    // Lista dos interesses do próprio utilizador para mostrar no filtro
    private val _meusInteresses = MutableStateFlow<List<String>>(emptyList())
    val meusInteresses: StateFlow<List<String>> = _meusInteresses.asStateFlow()

    init {
        loadProfiles()
    }

    // Função para aplicar todos os filtros de uma vez
    fun aplicarFiltros(minIdade: Int, maxIdade: Int, interesse: String, sexualidade: String) {
        filtroIdadeMinima = minIdade
        filtroIdadeMaxima = maxIdade
        filtroInteresseFocado = interesse
        filtroSexualidade = sexualidade
        reloadProfiles()
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

                // 1. Buscar os dados do utilizador logado
                val currentUserDoc = firestore.collection("usuarios").document(currentUser.uid).get().await()

                val meuInteresse = currentUserDoc.getString("interesse")
                val minhasSubcategorias = currentUserDoc.get("subcategorias") as? List<String> ?: emptyList()
                val meusOutrosInteresses = currentUserDoc.get("outrosInteresses") as? List<String> ?: emptyList()

                val meusGostos = mutableSetOf<String>()
                meuInteresse?.let { if (it.isNotBlank()) meusGostos.add(it) }
                meusGostos.addAll(minhasSubcategorias.filter { it.isNotBlank() })
                meusGostos.addAll(meusOutrosInteresses.filter { it.isNotBlank() })

                // Atualiza a lista de interesses na UI para o filtro
                _meusInteresses.value = meusGostos.toList()

                // 2. Buscar outros perfis
                val querySnapshot = firestore.collection("usuarios").get().await()
                val avaliadosSnapshot = firestore.collection("conexoes")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                val perfilsAvaliados = avaliadosSnapshot.documents.map { it.getString("likedUserId") }.toSet()
                val profiles = mutableListOf<Profile>()

                for (document in querySnapshot.documents) {
                    if (document.id == currentUser.uid) continue
                    if (perfilsAvaliados.contains(document.id)) continue

                    val idade = document.getLong("idade")?.toInt() ?: continue

                    // 🎯 FILTRO 1: Idade
                    if (idade < filtroIdadeMinima || idade > filtroIdadeMaxima) continue

                    val sexualidadePerfil = document.getString("sexualidade") ?: ""

                    // 🎯 FILTRO 2: Sexualidade (Verifica se está preenchido e se é diferente do escolhido)
                    if (filtroSexualidade.isNotEmpty() && !sexualidadePerfil.equals(filtroSexualidade, ignoreCase = true)) {
                        continue
                    }

                    // Obter os gostos deste outro utilizador
                    val outroInteresse = document.getString("interesse")
                    val outrasSubcategorias = document.get("subcategorias") as? List<String> ?: emptyList()
                    val outrosOutrosInteresses = document.get("outrosInteresses") as? List<String> ?: emptyList()

                    val outrosGostos = mutableSetOf<String>()
                    outroInteresse?.let { if (it.isNotBlank()) outrosGostos.add(it) }
                    outrosGostos.addAll(outrasSubcategorias.filter { it.isNotBlank() })
                    outrosGostos.addAll(outrosOutrosInteresses.filter { it.isNotBlank() })

                    // 🎯 FILTRO 3: Interesse Focado (Modo Nicho)
                    // Se o utilizador focou num interesse, e o outro perfil NÃO o tem, pulamos
                    if (filtroInteresseFocado.isNotEmpty() && !outrosGostos.contains(filtroInteresseFocado)) {
                        continue
                    }

                    // Regra base: Tem de ter pelo menos 1 gosto em comum geral
                    val gostosEmComum = meusGostos.intersect(outrosGostos)
                    if (gostosEmComum.isEmpty()) continue

                    val nome = document.getString("nome") ?: continue
                    val bio = document.getString("bio") ?: ""
                    val signo = document.getString("signo") ?: ""

                    val profile = Profile(
                        id = document.id,
                        name = nome,
                        imageUrl = "",
                        age = idade,
                        bio = if (bio.isNotEmpty()) bio else "Sem bio disponível",
                        sexualidade = sexualidadePerfil,
                        signo = signo,
                        interesses = outrosGostos.toList()
                    )

                    profiles.add(profile)
                }

                availableProfiles.clear()
                availableProfiles.addAll(profiles.shuffled())

                if (profiles.isNotEmpty()) {
                    _uiState.value = ConexoesUiState.Success(
                        currentProfile = availableProfiles.first(),
                        remainingProfiles = availableProfiles.size - 1,
                        currentIndex = 0,
                        totalProfiles = availableProfiles.size
                    )
                } else {
                    _uiState.value = ConexoesUiState.Empty
                }
            } catch (e: Exception) {
                _uiState.value = ConexoesUiState.Error(message = "Erro: ${e.message}")
            }
        }
    }

    fun likeProfile() {
        val currentProfile = availableProfiles.getOrNull(currentProfileIndex) ?: return
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val conexaoData = hashMapOf(
                    "userId" to currentUser.uid,
                    "likedUserId" to currentProfile.id,
                    "liked" to true,
                    "timestamp" to Date()
                )

                firestore.collection("conexoes").add(conexaoData).await()

                val mutualLikeQuery = firestore.collection("conexoes")
                    .whereEqualTo("userId", currentProfile.id)
                    .whereEqualTo("likedUserId", currentUser.uid)
                    .whereEqualTo("liked", true)
                    .get()
                    .await()

                if (mutualLikeQuery.documents.isNotEmpty()) {
                    val matchData = hashMapOf(
                        "participants" to listOf(currentUser.uid, currentProfile.id),
                        "timestamp" to Date(),
                        "lastMessage" to "",
                        "lastMessageTime" to null,
                        "isActive" to true
                    )

                    firestore.collection("matches").add(matchData).await()

                    _matchState.value = MatchState.NewMatch(currentProfile.name)
                    delay(3000)
                    _matchState.value = MatchState.None
                }

                delay(200)
                moveToNextProfile()
            } catch (e: Exception) {
                moveToNextProfile()
            }
        }
    }

    fun dislikeProfile() {
        val currentProfile = availableProfiles.getOrNull(currentProfileIndex) ?: return
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val conexaoData = hashMapOf(
                    "userId" to currentUser.uid,
                    "likedUserId" to currentProfile.id,
                    "liked" to false,
                    "timestamp" to Date()
                )
                firestore.collection("conexoes").add(conexaoData).await()
                delay(200)
                moveToNextProfile()
            } catch (e: Exception) {
                moveToNextProfile()
            }
        }
    }

    fun nextProfile() {
        if (currentProfileIndex < availableProfiles.size - 1) {
            currentProfileIndex++
            updateUiState()
        }
    }

    fun previousProfile() {
        if (currentProfileIndex > 0) {
            currentProfileIndex--
            updateUiState()
        }
    }

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
        updateUiState()
    }

    fun canGoBack(): Boolean = currentProfileIndex > 0
    fun canGoForward(): Boolean = currentProfileIndex < availableProfiles.size - 1

    fun reloadProfiles() {
        currentProfileIndex = 0
        availableProfiles.clear()
        _uiState.value = ConexoesUiState.Loading
        loadProfiles()
    }

    fun clearMatchState() {
        _matchState.value = MatchState.None
    }
}

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

sealed class MatchState {
    object None : MatchState()
    data class NewMatch(val matchedUserName: String) : MatchState()
}