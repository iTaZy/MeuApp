package com.tazy.meuapp.model

import java.util.Date

data class Match(
    val id: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val user1Name: String = "",
    val user2Name: String = "",
    val timestamp: Date = Date(),
    val lastMessage: String = "",
    val lastMessageTime: Date? = null,
    val isActive: Boolean = true,
    val lastMessageSenderId: String = "",
    val isLastMessageRead: Boolean = true,
    val arquivadosPor: List<String> = emptyList(),
    val nicknames: Map<String, String> = emptyMap() // NOVO: Mapeamento de apelidos
) {
    fun getOtherUserName(currentUserId: String): String {
        val defaultName = if (currentUserId == user1Id) user2Name else user1Name
        // Se houver um apelido definido por você para este match, retorna ele. Senão, o nome original.
        return nicknames[currentUserId] ?: defaultName
    }

    fun getOtherUserId(currentUserId: String): String {
        return if (currentUserId == user1Id) user2Id else user1Id
    }
}