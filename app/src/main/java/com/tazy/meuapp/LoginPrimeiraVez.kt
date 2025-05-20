package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun LoginPrimeiraVez(
    navController: NavController
) {
    var bio by remember { mutableStateOf("") }

    val interessesLista = listOf(
        "Esportes", "Leitura", "Música", "Filmes", "Viagens", "Culinária",
        "Tecnologia", "Jogos", "Arte", "Fotografia"
    )

    val interessesSelecionados = remember { mutableStateListOf<String>() }

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Text(
            text = "Complete seu perfil",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Escreva uma bio") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Selecione seus interesses:", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(interessesLista.size) { index ->
                val interesse = interessesLista[index]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = interessesSelecionados.contains(interesse),
                            onValueChange = {
                                if (it) {
                                    interessesSelecionados.add(interesse)
                                } else {
                                    interessesSelecionados.remove(interesse)
                                }
                            }
                        )
                        .padding(8.dp)
                ) {
                    Checkbox(
                        checked = interessesSelecionados.contains(interesse),
                        onCheckedChange = null
                    )
                    Text(text = interesse, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Button(
            onClick = {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    // Aqui você pode mostrar um erro ou redirecionar para login
                    return@Button
                }

                val dadosUsuario = hashMapOf(
                    "bio" to bio,
                    "interesses" to interessesSelecionados.toList()
                )

                firestore.collection("usuarios")
                    .document(currentUser.uid)
                    .set(dadosUsuario, SetOptions.merge()) // Aqui faz merge
                    .addOnSuccessListener {
                        // Navega para próxima tela depois do cadastro completo
                        navController.navigate("telaPrincipal") {
                            popUpTo("loginPrimeiraVez") { inclusive = true }
                        }
                    }
                    .addOnFailureListener {
                        // Aqui você pode mostrar uma mensagem de erro para o usuário
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar")
        }
    }
}
