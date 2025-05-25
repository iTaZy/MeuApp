package com.tazy.meuapp

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tazy.meuapp.model.CabecalhoUsuario
import com.tazy.meuapp.model.RodapeUsuario

@Composable
fun LoginPrimeiraVez(
    navController: NavController,
    modoEdicao: Boolean = false
) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Estados do formulário
    var bio by remember { mutableStateOf("") }
    var idade by remember { mutableStateOf("") }
    var nomeUsuario by remember { mutableStateOf("") }
    var codigoCondominio by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(true) }

    // Lista de interesses
    val interessesLista = listOf(
        "Esportes", "Leitura", "Música", "Filmes", "Viagens",
        "Culinária", "Tecnologia", "Jogos", "Arte", "Fotografia"
    )
    val interessesSelecionados = remember { mutableStateListOf<String>() }

    // Cores e ícones
    val azul = Color(0xFF2196F3)
    val icones: Map<String, ImageVector> = mapOf(
        "Esportes" to Icons.Default.SportsSoccer,
        "Leitura" to Icons.Default.MenuBook,
        "Música" to Icons.Default.MusicNote,
        "Filmes" to Icons.Default.Movie,
        "Viagens" to Icons.Default.Flight,
        "Culinária" to Icons.Default.RestaurantMenu,
        "Tecnologia" to Icons.Default.Devices,
        "Jogos" to Icons.Default.SportsEsports,
        "Arte" to Icons.Default.Brush,
        "Fotografia" to Icons.Default.PhotoCamera
    )

    // Carregar dados existentes
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            firestore.collection("usuarios")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    nomeUsuario = document.getString("nome") ?: currentUser.displayName ?: "Usuário"
                    codigoCondominio = document.getString("codigoCondominio") ?: "Condomínio"
                    bio = document.getString("bio") ?: ""
                    idade = document.get("idade")?.toString() ?: ""

                    val interesses = document.get("interesses") as? List<String> ?: emptyList()
                    interessesSelecionados.addAll(interesses)

                    carregando = false
                }
                .addOnFailureListener {
                    nomeUsuario = currentUser.displayName ?: "Usuário"
                    codigoCondominio = "Condomínio"
                    erro = if (modoEdicao) "Erro ao carregar perfil" else ""
                    carregando = false
                }
        } else {
            carregando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Cabeçalho do usuário com dados reais
        CabecalhoUsuario(
            state = TelaPrincipalState(
                nomeUsuario = nomeUsuario,
                codigoCondominio = codigoCondominio
            ),
            navController = navController
        )

        // Conteúdo principal com scroll
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            if (carregando) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                )
            } else {
                if (!modoEdicao) {
                    Text(
                        text = "Complete seu perfil",
                        fontSize = 22.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Campo de idade
                OutlinedTextField(
                    value = idade,
                    onValueChange = { idade = it },
                    label = { Text("Idade") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )

                // Campo de bio
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Selecione seus interesses:",
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Lista de interesses com altura limitada
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        items(interessesLista.size) { index ->
                            val interesse = interessesLista[index]
                            val isSelecionado = interessesSelecionados.contains(interesse)
                            val icone = icones[interesse] ?: Icons.Default.Star

                            val offset by animateDpAsState(
                                targetValue = if (isSelecionado) 32.dp else 0.dp,
                                animationSpec = tween(durationMillis = 250),
                                label = "offsetAnim"
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .toggleable(
                                        value = isSelecionado,
                                        onValueChange = {
                                            if (it) interessesSelecionados.add(interesse)
                                            else interessesSelecionados.remove(interesse)
                                        }
                                    )
                            ) {
                                Icon(
                                    imageVector = icone,
                                    contentDescription = interesse,
                                    tint = if (isSelecionado) azul else Color.Black,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(end = 12.dp)
                                )

                                Text(
                                    text = interesse,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                Box(
                                    modifier = Modifier
                                        .width(64.dp)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (isSelecionado) azul else Color.White)
                                        .border(2.dp, azul, RoundedCornerShape(50))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = offset)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(2.dp, azul, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                if (erro.isNotEmpty()) {
                    Text(
                        text = erro,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Botão fixo na parte inferior
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = {
                    if (currentUser == null) {
                        erro = "Usuário não autenticado"
                        return@Button
                    }

                    if (idade.isBlank()) {
                        erro = "Por favor, informe sua idade"
                        return@Button
                    }

                    val idadeNumero = try {
                        idade.toInt()
                    } catch (e: NumberFormatException) {
                        erro = "Idade inválida"
                        return@Button
                    }

                    if (idadeNumero <= 0 || idadeNumero > 120) {
                        erro = "Idade deve estar entre 1 e 120 anos"
                        return@Button
                    }

                    val dados = hashMapOf(
                        "nome" to nomeUsuario,
                        "codigoCondominio" to codigoCondominio,
                        "bio" to bio,
                        "idade" to idadeNumero,
                        "interesses" to interessesSelecionados.toList()
                    )

                    firestore.collection("usuarios")
                        .document(currentUser.uid)
                        .set(dados, SetOptions.merge())
                        .addOnSuccessListener {
                            if (modoEdicao) {
                                navController.popBackStack()
                            } else {
                                navController.navigate("lobbyPrincipal") {
                                    popUpTo("loginPrimeiraVez") { inclusive = true }
                                }
                            }
                        }
                        .addOnFailureListener {
                            erro = "Erro ao salvar alterações"
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = azul)
            ) {
                Text(
                    text = if (modoEdicao) "Salvar Alterações" else "Finalizar Cadastro",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }

        // Rodapé do usuário
        RodapeUsuario(navController = navController, selected = "")
    }
}