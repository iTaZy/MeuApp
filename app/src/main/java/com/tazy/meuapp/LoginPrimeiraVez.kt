package com.tazy.meuapp

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = interessesSelecionados.contains(interesse),
                            onValueChange = {
                                if (it) interessesSelecionados.add(interesse)
                                else interessesSelecionados.remove(interesse)
                            }
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = interessesSelecionados.contains(interesse),
                        onCheckedChange = null
                    )
                    Text(text = interesse, modifier = Modifier.padding(start = 8.dp))
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (modoEdicao) "Salvar Alterações" else "Finalizar Cadastro")
        }
    }
}