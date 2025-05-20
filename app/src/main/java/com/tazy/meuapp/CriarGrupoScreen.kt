package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    var interesse by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

    val interessesDisponiveis = listOf(
        "Esportes", "Leitura", "Música", "Filmes", "Viagens",
        "Culinária", "Tecnologia", "Jogos", "Arte", "Fotografia"
    )

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar novo grupo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Campo do nome
                OutlinedTextField(
                    value = nomeGrupo,
                    onValueChange = { nomeGrupo = it },
                    label = { Text("Nome do Grupo*") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo da descrição
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dropdown de interesses
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        value = interesse,
                        onValueChange = {},
                        label = { Text("Selecione um interesse*") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        interessesDisponiveis.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    interesse = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                erro?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (nomeGrupo.isBlank() || interesse.isBlank()) {
                            erro = "Preencha todos os campos obrigatórios"
                        } else {
                            criarGrupo(
                                nomeGrupo = nomeGrupo,
                                descricao = descricao,
                                interesse = interesse,
                                firestore = firestore,
                                auth = auth,
                                navController = navController,
                                scope = scope,
                                onError = { erro = it },
                                onLoading = { carregando = it }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !carregando
                ) {
                    if (carregando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Criar Grupo")
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
            // Buscar o código do condomínio do usuário no Firestore
            val usuarioDoc = firestore.collection("usuarios")
                .document(usuarioAtual.uid)
                .get()
                .await()

            val codigoCondominio = usuarioDoc.getString("codigoCondominio")
                ?: run {
                    onError("Código do condomínio não encontrado para o usuário")
                    onLoading(false)
                    return@launch
                }

            val grupoData = hashMapOf(
                "nome" to nomeGrupo,
                "descricao" to descricao,
                "interesses" to listOf(interesse),
                "criadorId" to usuarioAtual.uid,
                "participantes" to listOf(usuarioAtual.uid),
                "membrosCount" to 1,
                "relevancia" to 3,
                "ultimaMensagem" to "",
                "mensagensNaoLidas" to 0,
                "dataCriacao" to FieldValue.serverTimestamp(),
                "codigoCondominio" to codigoCondominio  // <-- adiciona aqui
            )

            val docRef = firestore.collection("grupos").document()
            docRef.set(grupoData).await()

            // Cria subcoleção de leitura
            docRef.collection("leituraUsuarios")
                .document(usuarioAtual.uid)
                .set(mapOf("ultimaLeitura" to FieldValue.serverTimestamp()))
                .await()

            // Atualiza usuário
            firestore.collection("usuarios")
                .document(usuarioAtual.uid)
                .update("grupos", FieldValue.arrayUnion(docRef.id))
                .await()

            navController.popBackStack()
        } catch (e: Exception) {
            onError(e.message ?: "Erro ao criar grupo")
        } finally {
            onLoading(false)
        }
    }
}
