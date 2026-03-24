// Match.kt
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
    // NOVOS CAMPOS AQUI:
    val lastMessageSenderId: String = "",
    val isLastMessageRead: Boolean = true
) {
    fun getOtherUserName(currentUserId: String): String {
        return if (currentUserId == user1Id) user2Name else user1Name
    }

    fun getOtherUserId(currentUserId: String): String {
        return if (currentUserId == user1Id) user2Id else user1Id
    }
}