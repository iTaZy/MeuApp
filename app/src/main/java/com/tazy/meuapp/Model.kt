package com.tazy.meuapp

data class Mensagem(
    val id: String = "",
    val texto: String = "",
    val remetenteId: String = "",
    val remetenteNome: String = "",
    val timestamp: Long = 0L
)
data class Chat(
    val id: String,
    val nome: String,
    val ultimaMensagem: String = "",
    val mensagensNaoLidas: Int = 0
)

data class GrupoRecomendado(
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val relevancia: Int = 3,
    val membrosCount: Int = 1,
    // 👇 Adicione apenas esta linha na classe que já existe aí:
    val interesses: List<String> = emptyList()
)


data class ChatState(
    val mensagens: List<Mensagem> = emptyList(),
    val gruposRecomendados: List<GrupoRecomendado> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null,
    val nomeGrupo: String = ""
)
