package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (modoEdicao) "Editar Perfil" else "Complete seu perfil",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Selecione seus interesses:", fontSize = 18.sp)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(interessesLista.size) { index ->
                val interesse = interessesLista[index]
                val isSelecionado = interessesSelecionados.contains(interesse)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(if (isSelecionado) Color(0xFF2196F3) else Color.White)
                        .border(
                            width = 1.dp,
                            color = Color(0xFF2196F3),
                            shape = RoundedCornerShape(25.dp)
                        )
                        .toggleable(
                            value = isSelecionado,
                            onValueChange = {
                                if (it) interessesSelecionados.add(interesse)
                                else interessesSelecionados.remove(interesse)
                            }
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelecionado) Color.White else Color(0xFF2196F3)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isSelecionado) Color(0xFF2196F3) else Color.White,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = interesse,
                            color = if (isSelecionado) Color.White else Color.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        if (erro.isNotEmpty()) {
            Text(
                text = erro,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

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
                .padding(top = 16.dp)
        ) {
            Text(if (modoEdicao) "Salvar Alterações" else "Finalizar Cadastro")
        }
    }
}
