// ChatMessage.kt
package com.tazy.meuapp.model

import java.util.Date

data class ChatMessage(
    val id: String = "",
    val matchId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Date = Date(),
    val isRead: Boolean = false
)

// Estados para o ViewModel
sealed class MatchesUiState {
    object Loading : MatchesUiState()
    data class Success(val matches: List<Match>) : MatchesUiState()
    data class Error(val message: String) : MatchesUiState()
    object Empty : MatchesUiState()
}
