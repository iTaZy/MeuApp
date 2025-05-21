package com.tazy.meuapp

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun LoginPrimeiraVez(
    navController: NavController,
    modoEdicao: Boolean = false
) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var bio by remember { mutableStateOf("") }
    val interessesLista = listOf(
        "Esportes", "Leitura", "Música", "Filmes", "Viagens",
        "Culinária", "Tecnologia", "Jogos", "Arte", "Fotografia"
    )
    val interessesSelecionados = remember { mutableStateListOf<String>() }
    var erro by remember { mutableStateOf("") }

    // Carregar dados existentes se for edição
    LaunchedEffect(Unit) {
        if (modoEdicao && currentUser != null) {
            firestore.collection("usuarios")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    bio = document.getString("bio") ?: ""
                    val interesses = document.get("interesses") as? List<String> ?: emptyList()
                    interessesSelecionados.addAll(interesses)
                }
                .addOnFailureListener {
                    erro = "Erro ao carregar perfil"
                }
        }
    }

    val azul = Color(0xFF2196F3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Cabeçalho azul
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(azul)
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (modoEdicao) "Editar Perfil" else "Complete seu perfil",
                fontSize = 22.sp,
                color = Color.White
            )
        }

        Column(modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxSize()
        ) {

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

            // Caixa com sombra para os interesses
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                LazyColumn {
                    items(interessesLista.size) { index ->
                        val interesse = interessesLista[index]
                        val isSelecionado = interessesSelecionados.contains(interesse)

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
                            // Trilha do switch
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

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = interesse,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
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

            Button(
                onClick = {
                    if (currentUser == null) {
                        erro = "Usuário não autenticado"
                        return@Button
                    }

                    val dados = hashMapOf(
                        "bio" to bio,
                        "interesses" to interessesSelecionados.toList()
                    )

                    firestore.collection("usuarios")
                        .document(currentUser.uid)
                        .set(dados, SetOptions.merge())
                        .addOnSuccessListener {
                            if (modoEdicao) {
                                navController.popBackStack()
                            } else {
                                navController.navigate("telaPrincipal") {
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
    }
}
