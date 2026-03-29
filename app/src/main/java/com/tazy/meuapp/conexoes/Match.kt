package com.tazy.meuapp.model

import java.util.Date

data class Match(
    val id: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val user1Name: String = "",
    val user2Name: String = "",
    val user1ImageUrl: String = "", // ✨ NOVO: Foto do usuário 1
    val user2ImageUrl: String = "", // ✨ NOVO: Foto do usuário 2
    val timestamp: Date = Date(),
    val lastMessage: String = "",
    val lastMessageTime: Date? = null,
    val isActive: Boolean = true,
    val lastMessageSenderId: String = "",
    val isLastMessageRead: Boolean = true,
    val arquivadosPor: List<String> = emptyList(),
    val nicknames: Map<String, String> = emptyMap()
) {
    fun getOtherUserName(currentUserId: String): String {
        val defaultName = if (currentUserId == user1Id) user2Name else user1Name
        return nicknames[currentUserId] ?: defaultName
    }

    fun getOtherUserId(currentUserId: String): String {
        return if (currentUserId == user1Id) user2Id else user1Id
    }

    // ✨ NOVA FUNÇÃO: Pega a foto da pessoa com quem você está conversando
    fun getOtherUserImageUrl(currentUserId: String): String {
        return if (currentUserId == user1Id) user2ImageUrl else user1ImageUrl
    }
}