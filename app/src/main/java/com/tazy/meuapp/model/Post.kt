package com.tazy.meuapp.model

import com.google.firebase.Timestamp

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,
    val likesCount: Int = 0,
    val likedByUser: Boolean = false,
    val codigoCondominio: String = ""
)
