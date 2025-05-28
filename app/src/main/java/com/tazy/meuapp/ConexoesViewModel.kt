// ConexoesViewModel.kt
package com.tazy.meuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tazy.meuapp.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ConexoesViewModel : ViewModel() {
    // Estado da tela
    private val _uiState = MutableStateFlow<ConexoesUiState>(ConexoesUiState.Loading)
    val uiState: StateFlow<ConexoesUiState> = _uiState.asStateFlow()

    // Lista de perfis disponíveis
    private val availableProfiles = mutableListOf<Profile>()

    // Índice do perfil atual
    private var currentProfileIndex = 0

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            try {
                // Simula um tempo de carregamento
                delay(1000)

                // Perfis simulados com apenas Nome, Bio e Idade
                val fakeProfiles = listOf(
                    Profile(
                        id = "1",
                        name = "Helena",
                        imageUrl = "", // Não usado, será placeholder
                        age = 24,
                        bio = "Apaixonada por café, boas conversas e gente que sabe rir de si mesma. Coração grande, playlist melhor ainda. Se você for legal, talvez ganhe um pedaço da minha sobremesa"
                    ),
                    Profile(
                        id = "2",
                        name = "Carlos",
                        imageUrl = "", // Não usado, será placeholder
                        age = 35,
                        bio = "Corredor de fim de semana, sempre no parque aos sábados. Procuro alguém para compartilhar aventuras e cafés da manhã prolongados"
                    ),
                    Profile(
                        id = "3",
                        name = "Mariana",
                        imageUrl = "", // Não usado, será placeholder
                        age = 31,
                        bio = "Organizo eventos no condomínio e adoro cozinhar. Se você aguenta meus experimentos culinários, podemos ser amigos"
                    ),
                    Profile(
                        id = "4",
                        name = "Rafael",
                        imageUrl = "", // Não usado, será placeholder
                        age = 28,
                        bio = "Desenvolvedor por dia, guitarrista por noite. Procuro alguém que goste de música e não se importe com o barulho dos ensaios"
                    ),
                    Profile(
                        id = "5",
                        name = "Ana",
                        imageUrl = "", // Não usado, será placeholder
                        age = 26,
                        bio = "Veterinária e defensora dos animais. Tenho 3 gatos e um coração gigante. Se você não gosta de pets, deslize para o lado"
                    )
                )

                availableProfiles.addAll(fakeProfiles)

                if (fakeProfiles.isNotEmpty()) {
                    _uiState.value = ConexoesUiState.Success(
                        currentProfile = fakeProfiles.first(),
                        remainingProfiles = fakeProfiles.size - 1
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
        viewModelScope.launch {
            // Aqui você implementaria a lógica para registrar o like no backend
            // Por exemplo: api.likeProfile(currentProfile.id)

            // Simula um pequeno delay para feedback visual
            delay(200)

            // Avança para o próximo perfil
            moveToNextProfile()
        }
    }

    fun dislikeProfile() {
        val currentProfile = availableProfiles.getOrNull(currentProfileIndex) ?: return
        viewModelScope.launch {
            // Aqui você implementaria a lógica para registrar o dislike no backend
            // Por exemplo: api.dislikeProfile(currentProfile.id)

            // Simula um pequeno delay para feedback visual
            delay(200)

            // Avança para o próximo perfil
            moveToNextProfile()
        }
    }

    private fun moveToNextProfile() {
        currentProfileIndex++
        if (currentProfileIndex < availableProfiles.size) {
            _uiState.value = ConexoesUiState.Success(
                currentProfile = availableProfiles[currentProfileIndex],
                remainingProfiles = availableProfiles.size - currentProfileIndex - 1
            )
        } else {
            _uiState.value = ConexoesUiState.Empty
        }
    }

    // Função para recarregar perfis (útil para pull-to-refresh)
    fun reloadProfiles() {
        currentProfileIndex = 0
        availableProfiles.clear()
        _uiState.value = ConexoesUiState.Loading
        loadProfiles()
    }
}

// Estados possíveis da UI
sealed class ConexoesUiState {
    object Loading : ConexoesUiState()
    data class Success(
        val currentProfile: Profile,
        val remainingProfiles: Int
    ) : ConexoesUiState()
    data class Error(val message: String) : ConexoesUiState()
    object Empty : ConexoesUiState()
}