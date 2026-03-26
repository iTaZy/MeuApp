package com.tazy.meuapp.model

import com.google.firebase.Timestamp

data class Comment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
)