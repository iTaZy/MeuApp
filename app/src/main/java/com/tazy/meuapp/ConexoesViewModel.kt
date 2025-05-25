// ConexoesViewModel.kt
package com.tazy.meuapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tazy.meuapp.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                // Simulando carregamento de dados (substitua pela sua lógica real)
                val fakeProfiles = listOf(
                    Profile(
                        id = "1",
                        name = "Ana Silva",
                        imageUrl = "https://randomuser.me/api/portraits/women/42.jpg",
                        age = 28,
                        bio = "Mora no bloco B, ama animais e jardinagem"
                    ),
                    Profile(
                        id = "2",
                        name = "Carlos Oliveira",
                        imageUrl = "https://randomuser.me/api/portraits/men/32.jpg",
                        age = 35,
                        bio = "Corredor de fim de semana, sempre no parque aos sábados"
                    ),
                    Profile(
                        id = "3",
                        name = "Mariana Costa",
                        imageUrl = "https://randomuser.me/api/portraits/women/63.jpg",
                        age = 31,
                        bio = "Organiza eventos no condomínio, adora cozinhar"
                    )
                )

                availableProfiles.addAll(fakeProfiles)
                _uiState.value = ConexoesUiState.Success(
                    currentProfile = fakeProfiles.first(),
                    remainingProfiles = fakeProfiles.size - 1
                )
            } catch (e: Exception) {
                _uiState.value = ConexoesUiState.Error(
                    message = "Erro ao carregar perfis: ${e.localizedMessage}"
                )
            }
        }
    }

    fun likeProfile() {
        val currentProfile = availableProfiles.getOrNull(currentProfileIndex) ?: return
        viewModelScope.launch {
            // Aqui você implementaria a lógica para registrar o like no backend
            // Por enquanto só avançamos para o próximo perfil
            moveToNextProfile()
        }
    }

    fun dislikeProfile() {
        val currentProfile = availableProfiles.getOrNull(currentProfileIndex) ?: return
        viewModelScope.launch {
            // Aqui você implementaria a lógica para registrar o dislike no backend
            // Por enquanto só avançamos para o próximo perfil
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