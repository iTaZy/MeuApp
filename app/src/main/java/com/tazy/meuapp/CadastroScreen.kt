package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun CadastroScreen(
    onCadastroFinalizado: () -> Unit,
    onCadastroPrimeiroLogin: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var codigoCondominio by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2196F3)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Criar Conta",
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedCampo("Nome", nome) { nome = it }
            OutlinedCampo("E-mail", email) { email = it }
            OutlinedCampo("Senha", senha, isPassword = true) { senha = it }
            OutlinedCampo("Código do Condomínio", codigoCondominio) { codigoCondominio = it.uppercase() }

            erro?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    erro = null
                    scope.launch {
                        try {
                            if (nome.isBlank() || email.isBlank() || senha.isBlank() || codigoCondominio.isBlank()) {
                                erro = "Por favor, preencha todos os campos"
                                return@launch
                            }

                            // Verificar se código do condomínio existe
                            val doc = firestore.collection("condominios")
                                .document(codigoCondominio)
                                .get()
                                .await()

                            if (!doc.exists()) {
                                erro = "Código do condomínio inválido"
                                return@launch
                            }

                            // Criar usuário no Firebase Auth
                            val result = auth.createUserWithEmailAndPassword(email, senha).await()
                            val uid = result.user?.uid ?: run {
                                erro = "Falha ao criar usuário"
                                return@launch
                            }

                            // Salvar dados do usuário no Firestore usando merge para não sobrescrever
                            val usuario = hashMapOf(
                                "nome" to nome,
                                "email" to email,
                                "codigoCondominio" to codigoCondominio
                            )
                            firestore.collection("usuarios")
                                .document(uid)
                                .set(usuario, SetOptions.merge()) // Aqui merge para mesclar dados
                                .await()

                            onCadastroPrimeiroLogin()
                        } catch (e: Exception) {
                            erro = e.localizedMessage ?: "Erro desconhecido"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Cadastrar", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Voltar para login",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    onCadastroFinalizado()
                }
            )
        }
    }
}

@Composable
fun OutlinedCampo(
    label: String,
    value: String,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White
        )
    )
}
