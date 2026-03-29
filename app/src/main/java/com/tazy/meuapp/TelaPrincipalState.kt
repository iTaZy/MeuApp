package com.tazy.meuapp

data class TelaPrincipalState(
    val nomeUsuario: String? = null,
    val fotoPerfilUrl: String? = null, // ✨ AQUI ESTÁ O CAMPO DA FOTO!
    val codigoCondominio: String = "",
    val chatsAtivos: List<Chat> = emptyList(),
    val gruposRecomendados: List<GrupoRecomendado> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null
)