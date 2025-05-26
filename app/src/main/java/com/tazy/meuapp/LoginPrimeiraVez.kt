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
import androidx.compose.ui.text.font.FontWeight
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
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var idade by remember { mutableStateOf("") }
    var sexualidade by remember { mutableStateOf("") }
    var signo by remember { mutableStateOf("") }
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

    // Cores
    val azul = Color(0xFF2196F3)
    val cinzaClaro = Color(0xFFF5F5F5)

    // Ícones para interesses
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
                    nome = document.getString("nome") ?: ""
                    email = document.getString("email") ?: currentUser.email ?: ""
                    bio = document.getString("bio") ?: ""
                    idade = document.get("idade")?.toString() ?: ""
                    sexualidade = document.getString("sexualidade") ?: ""
                    signo = document.getString("signo") ?: ""

                    val interesses = document.get("interesses") as? List<String> ?: emptyList()
                    interessesSelecionados.addAll(interesses)

                    carregando = false
                }
                .addOnFailureListener {
                    nomeUsuario = currentUser.displayName ?: "Usuário"
                    codigoCondominio = "Condomínio"
                    email = currentUser.email ?: ""
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
        // Cabeçalho do usuário
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
                .padding(horizontal = 20.dp)
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
                Spacer(modifier = Modifier.height(16.dp))

                // Foto de perfil (placeholder)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .border(3.dp, azul, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.size(60.dp),
                        tint = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botão alterar foto
                Button(
                    onClick = { /* Implementar alteração de foto */ },
                    colors = ButtonDefaults.buttonColors(containerColor = azul),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Alterar foto de perfil", color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Campo Nome
                CampoFormulario(
                    valor = nome,
                    placeholder = "Nome",
                    onValueChange = { nome = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Email
                CampoFormulario(
                    valor = email,
                    placeholder = "Email",
                    onValueChange = { email = it },
                    enabled = false // Email não editável
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Linha com Idade e Sexualidade
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CampoFormulario(
                        valor = idade,
                        placeholder = "Idade",
                        onValueChange = { idade = it },
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number
                    )

                    CampoFormulario(
                        valor = sexualidade,
                        placeholder = "Sexualidade",
                        onValueChange = { sexualidade = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Signo
                CampoFormulario(
                    valor = signo,
                    placeholder = "Seu signo",
                    onValueChange = { signo = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Bio (maior)
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    placeholder = { Text("Bio", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = azul,
                        unfocusedContainerColor = cinzaClaro,
                        focusedContainerColor = cinzaClaro
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Título dos interesses
                Text(
                    "Selecione seus interesses",
                    fontSize = 16.sp,
                    color = azul,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Lista de interesses com switches
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cinzaClaro, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    interessesLista.chunked(2).forEach { par ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            par.forEach { interesse ->
                                val isSelecionado = interessesSelecionados.contains(interesse)
                                val icone = icones[interesse] ?: Icons.Default.Star

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 8.dp)
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
                                        tint = if (isSelecionado) azul else Color.Gray,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(end = 8.dp)
                                    )

                                    Text(
                                        text = interesse,
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Switch(
                                        checked = isSelecionado,
                                        onCheckedChange = {
                                            if (it) interessesSelecionados.add(interesse)
                                            else interessesSelecionados.remove(interesse)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = azul,
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color.Gray
                                        )
                                    )
                                }
                            }

                            // Se o par tem apenas um item, adiciona um espaço vazio
                            if (par.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                if (erro.isNotEmpty()) {
                    Text(
                        text = erro,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Botão fixo na parte inferior
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = {
                    if (currentUser == null) {
                        erro = "Usuário não autenticado"
                        return@Button
                    }

                    if (nome.isBlank()) {
                        erro = "Por favor, informe seu nome"
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
                        "nome" to nome,
                        "email" to email,
                        "codigoCondominio" to codigoCondominio,
                        "bio" to bio,
                        "idade" to idadeNumero,
                        "sexualidade" to sexualidade,
                        "signo" to signo,
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

@Composable
fun CampoFormulario(
    valor: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val cinzaClaro = Color(0xFFF5F5F5)
    val azul = Color(0xFF2196F3)

    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = azul,
            unfocusedContainerColor = cinzaClaro,
            focusedContainerColor = cinzaClaro,
            disabledContainerColor = cinzaClaro.copy(alpha = 0.6f),
            disabledTextColor = Color.Gray
        )
    )
}