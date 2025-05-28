// Profile.kt
package com.tazy.meuapp.model

data class Profile(
    val id: String,
    val name: String,
    val imageUrl: String = "",
    val age: Int,
    val bio: String = "",
    val sexualidade: String = "",
    val signo: String = "",
    val interesses: List<String> = emptyList()
)