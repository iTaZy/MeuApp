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

    // 👇 Trava de segurança para impedir cliques duplos rápidos
    private var isProcessing = false

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

    private val _meusInteresses = MutableStateFlow<List<String>>(emptyList())
    val meusInteresses: StateFlow<List<String>> = _meusInteresses.asStateFlow()

    init {
        loadProfiles()
    }

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
                val currentUser = auth.currentUser ?: run {
                    _uiState.value = ConexoesUiState.Error("Usuário não autenticado")
                    return@launch
                }

                val currentUserDoc = firestore.collection("usuarios").document(currentUser.uid).get().await()

                val meuInteresse = currentUserDoc.getString("interesse")
                val minhasSubcategorias = currentUserDoc.get("subcategorias") as? List<String> ?: emptyList()
                val meusOutrosInteresses = currentUserDoc.get("outrosInteresses") as? List<String> ?: emptyList()

                val meusGostos = mutableSetOf<String>()
                meuInteresse?.let { if (it.isNotBlank()) meusGostos.add(it) }
                meusGostos.addAll(minhasSubcategorias.filter { it.isNotBlank() })
                meusGostos.addAll(meusOutrosInteresses.filter { it.isNotBlank() })

                _meusInteresses.value = meusGostos.toList()

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

                    if (idade < filtroIdadeMinima || idade > filtroIdadeMaxima) continue

                    val sexualidadePerfil = document.getString("sexualidade") ?: ""

                    if (filtroSexualidade.isNotEmpty() && !sexualidadePerfil.equals(filtroSexualidade, ignoreCase = true)) {
                        continue
                    }

                    val outroInteresse = document.getString("interesse")
                    val outrasSubcategorias = document.get("subcategorias") as? List<String> ?: emptyList()
                    val outrosOutrosInteresses = document.get("outrosInteresses") as? List<String> ?: emptyList()

                    val outrosGostos = mutableSetOf<String>()
                    outroInteresse?.let { if (it.isNotBlank()) outrosGostos.add(it) }
                    outrosGostos.addAll(outrasSubcategorias.filter { it.isNotBlank() })
                    outrosGostos.addAll(outrosOutrosInteresses.filter { it.isNotBlank() })

                    if (filtroInteresseFocado.isNotEmpty() && !outrosGostos.contains(filtroInteresseFocado)) {
                        continue
                    }

                    val gostosEmComum = meusGostos.intersect(outrosGostos)
                    if (gostosEmComum.isEmpty()) continue

                    val nome = document.getString("nome") ?: continue
                    val bio = document.getString("bio") ?: ""
                    val signo = document.getString("signo") ?: ""

                    val profile = Profile(
                        id = document.id,
                        name = nome,
                        // 👇 AGORA ELE PUXA A FOTO DO BANCO:
                        imageUrl = document.getString("fotoPerfil") ?: "",
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
        if (isProcessing) return // 🛑 Bloqueia se já estiver processando um clique
        isProcessing = true

        val currentProfile = availableProfiles.getOrNull(currentProfileIndex) ?: run { isProcessing = false; return }
        val currentUser = auth.currentUser ?: run { isProcessing = false; return }

        viewModelScope.launch {
            try {
                // Salva a sua conexão no banco
                val conexaoData = hashMapOf(
                    "userId" to currentUser.uid,
                    "likedUserId" to currentProfile.id,
                    "liked" to true,
                    "timestamp" to Date()
                )
                firestore.collection("conexoes").add(conexaoData).await()

                // Verifica se a outra pessoa também te curtiu
                val mutualLikeQuery = firestore.collection("conexoes")
                    .whereEqualTo("userId", currentProfile.id)
                    .whereEqualTo("likedUserId", currentUser.uid)
                    .whereEqualTo("liked", true)
                    .get()
                    .await()

                if (mutualLikeQuery.documents.isNotEmpty()) {
                    // 🛑 VALIDAÇÃO DE MATCH DUPLICADO
                    val existingMatches = firestore.collection("matches")
                        .whereArrayContains("participants", currentUser.uid)
                        .get()
                        .await()

                    val matchJaExiste = existingMatches.documents.any { doc ->
                        val parts = doc.get("participants") as? List<String> ?: emptyList()
                        parts.contains(currentProfile.id)
                    }

                    // Só cria o match se ele não existir
                    if (!matchJaExiste) {
                        val matchData = hashMapOf(
                            "participants" to listOf(currentUser.uid, currentProfile.id),
                            "timestamp" to Date(),
                            "lastMessage" to "",
                            "lastMessageTime" to null,
                            "isActive" to true
                        )

                        // 👇 Salva e guarda a referência para pegar o ID do chat!
                        val docRef = firestore.collection("matches").add(matchData).await()

                        // Avisa a UI para mostrar o popup passando o perfil E o ID do chat!
                        _matchState.value = MatchState.NewMatch(currentProfile, docRef.id)
                    }
                }

                moveToNextProfile()
            } catch (e: Exception) {
                moveToNextProfile()
            } finally {
                isProcessing = false // ✅ Libera para o próximo clique
            }
        }
    }

    fun dislikeProfile() {
        if (isProcessing) return // 🛑 Bloqueia clique duplo
        isProcessing = true

        val currentProfile = availableProfiles.getOrNull(currentProfileIndex) ?: run { isProcessing = false; return }
        val currentUser = auth.currentUser ?: run { isProcessing = false; return }

        viewModelScope.launch {
            try {
                val conexaoData = hashMapOf(
                    "userId" to currentUser.uid,
                    "likedUserId" to currentProfile.id,
                    "liked" to false,
                    "timestamp" to Date()
                )
                firestore.collection("conexoes").add(conexaoData).await()
                moveToNextProfile()
            } catch (e: Exception) {
                moveToNextProfile()
            } finally {
                isProcessing = false // ✅ Libera o botão
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
    // 👇 Agora ele recebe também o matchId!
    data class NewMatch(val matchedProfile: Profile, val matchId: String) : MatchState()
}