package com.tazy.meuapp

data class TelaPrincipalState(
    val nomeUsuario: String = "",
    val codigoCondominio: String = "",
    val gruposRecomendados: List<GrupoRecomendado> = emptyList(),
    val chatsAtivos: List<Chat> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null
)