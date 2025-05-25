
package com.tazy.meuapp.model

data class Profile(
    val id: String,
    val name: String,
    val imageUrl: String,
    val age: Int,
    val bio: String,
    // Adicione outros campos conforme necessário
    val apartment: String = "",
    val interests: List<String> = emptyList()
)