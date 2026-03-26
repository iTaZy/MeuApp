package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarGrupoScreen(navController: NavController) {
    var nomeGrupo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    // Estados do 1º Dropdown (Interesse Principal)
    var interesse by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // Estados do 2º Dropdown (Sub-interesse)
    var subInteresse by remember { mutableStateOf("") }
    var subExpanded by remember { mutableStateOf(false) }

    var erro by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

    // Mapa relacionando os interesses principais com suas subcategorias
    val categoriasMap = mapOf(
        "Esportes" to listOf("Futebol", "Vôlei", "Basquete", "Natação", "Academia", "E-sports", "Outros"),
        "Leitura" to listOf("Ficção", "Romance", "Fantasia", "Mangá", "Desenvolvimento Pessoal", "Técnico", "Outros"),
        "Música" to listOf("Rock", "Pop", "Sertanejo", "Eletrônica", "Rap/Trap", "K-Pop", "Outros"),
        "Filmes" to listOf("Ação", "Terror", "Comédia", "Drama", "Sci-Fi", "Animação", "Outros"),
        "Viagens" to listOf("Mochilão", "Praia", "Montanha", "Internacional", "Ecoturismo", "Outros"),
        "Culinária" to listOf("Confeitaria", "Churrasco", "Vegano", "Comida Asiática", "Fitness", "Outros"),
        "Tecnologia" to listOf("Programação", "Hardware", "IA", "Gadgets", "Cybersecurity", "Outros"),
        "Jogos" to listOf("Valorant", "Lol", "Minecraft", "Console", "Mobile", "RPG de Mesa", "Outros"),
        "Arte" to listOf("Pintura", "Digital", "Desenho", "Escultura", "Outros"),
        "Fotografia" to listOf("Natureza", "Retrato", "Urbana", "Eventos", "Outros")
    )

    val interessesDisponiveis = categoriasMap.keys.toList()

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    // Paleta KLANCORE
    val BgDeep = Color(0xFF060B10)
    val BgMid = Color(0xFF0B1422)
    val AccentCyan = Color(0xFF4DD9E8)
    val AccentPurple = Color(0xFF8B5CF6)
    val AccentBlue = Color(0xFF3B82F6)
    val TextPrimary = Color(0xFFE8F4FF)
    val TextSecondary = Color(0xFF8BA8C0)
    val FieldBg = Color(0xFF0D1A2A).copy(alpha = 0.75f)
    val FieldBorder = Color(0xFFFFFFFF).copy(alpha = 0.18f)
    val gradientButton = Brush.linearGradient(listOf(AccentPurple, AccentBlue, AccentCyan))

    // Borda arredondada suave para os campos
    val fieldShape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgMid, Color(0xFF050A0F))))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Criar novo grupo", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = AccentCyan)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Campo do nome
                OutlinedTextField(
                    value = nomeGrupo,
                    onValueChange = { nomeGrupo = it },
                    label = { Text("Nome do Grupo*", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = FieldBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentCyan,
                        focusedContainerColor = FieldBg,
                        unfocusedContainerColor = FieldBg
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo da descrição
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = FieldBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentCyan,
                        focusedContainerColor = FieldBg,
                        unfocusedContainerColor = FieldBg
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 1º Dropdown: Interesses Principais
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        readOnly = true,
                        value = interesse,
                        onValueChange = {},
                        shape = fieldShape,
                        label = { Text("Selecione um interesse principal*", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = FieldBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedTrailingIconColor = AccentCyan,
                            unfocusedTrailingIconColor = TextSecondary,
                            focusedContainerColor = FieldBg,
                            unfocusedContainerColor = FieldBg
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(BgMid)
                    ) {
                        interessesDisponiveis.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item, color = TextPrimary) },
                                onClick = {
                                    interesse = item
                                    expanded = false
                                    subInteresse = "" // Reseta o sub-interesse se trocar a categoria principal!
                                }
                            )
                        }
                    }
                }

                // 2º Dropdown: Sub-interesses
                if (interesse.isNotEmpty()) {
                    val subcategorias = categoriasMap[interesse] ?: emptyList()

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = subExpanded,
                        onExpandedChange = { subExpanded = !subExpanded }
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            readOnly = true,
                            value = subInteresse,
                            onValueChange = {},
                            shape = fieldShape,
                            label = { Text("Selecione um sub-interesse*", color = TextSecondary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = FieldBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedTrailingIconColor = AccentCyan,
                                unfocusedTrailingIconColor = TextSecondary,
                                focusedContainerColor = FieldBg,
                                unfocusedContainerColor = FieldBg
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = subExpanded,
                            onDismissRequest = { subExpanded = false },
                            modifier = Modifier.background(BgMid)
                        ) {
                            subcategorias.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = TextPrimary) },
                                    onClick = {
                                        subInteresse = item
                                        subExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                erro?.let {
                    Text(text = it, color = Color(0xFFFF5252), modifier = Modifier.padding(top = 16.dp), fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(if (carregando) androidx.compose.ui.graphics.SolidColor(Color.Gray) else gradientButton)
                        .clickable(enabled = !carregando) {
                            if (nomeGrupo.isBlank() || interesse.isBlank() || subInteresse.isBlank()) {
                                erro = "Preencha todos os campos obrigatórios"
                            } else {
                                criarGrupo(
                                    nomeGrupo, descricao, interesse, subInteresse,
                                    firestore, auth, navController, scope,
                                    { erro = it }, { carregando = it }
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (carregando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Criar Grupo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun criarGrupo(
    nomeGrupo: String,
    descricao: String,
    interesse: String,
    subInteresse: String,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth,
    navController: NavController,
    scope: CoroutineScope,
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    val usuarioAtual = auth.currentUser ?: run {
        onError("Usuário não autenticado")
        return
    }

    onLoading(true)

    scope.launch {
        try {
            val grupoData = hashMapOf(
                "nome" to nomeGrupo,
                "descricao" to descricao,
                "interesses" to listOf(interesse, subInteresse),
                "criadorId" to usuarioAtual.uid,
                "participantes" to listOf(usuarioAtual.uid),
                "membrosCount" to 1,
                "relevancia" to 3,
                "ultimaMensagem" to "",
                "mensagensNaoLidas" to 0,
                "dataCriacao" to FieldValue.serverTimestamp()
            )

            val docRef = firestore.collection("grupos").document()
            docRef.set(grupoData).await()

            docRef.collection("leituraUsuarios").document(usuarioAtual.uid)
                .set(mapOf("ultimaLeitura" to FieldValue.serverTimestamp())).await()

            firestore.collection("usuarios").document(usuarioAtual.uid)
                .update("grupos", FieldValue.arrayUnion(docRef.id)).await()

            navController.popBackStack()
        } catch (e: Exception) {
            onError(e.message ?: "Erro ao criar grupo")
        } finally {
            onLoading(false)
        }
    }
}