package com.tazy.meuapp

data class TelaPrincipalState(
    val nomeUsuario: String? = null,
    val codigoCondominio: String = "", // Pode deixar assim, só garantindo que não quebre
    val chatsAtivos: List<Chat> = emptyList(),
    val gruposRecomendados: List<GrupoRecomendado> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null
)