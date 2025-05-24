package com.tazy.meuapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.tazy.meuapp.ui.theme.MeuAppTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeuAppTheme {
                NavGraph()
            }
        }
    }
}

@Composable
fun LoginScreen(
    onCriarConta: () -> Unit,
    onLoginSucesso: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2196F3)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "MeuApp",
                fontSize = 36.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("E-mail", color = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                placeholder = { Text("Senha", color = Color.White.copy(alpha = 0.7f)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )

            Text(
                text = "Esqueci minha senha",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp, bottom = 24.dp)
                    .clickable { /* TODO: Implementar recuperação */ },
                fontSize = 14.sp
            )

            erro?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (email.isBlank() || senha.isBlank()) {
                        erro = "Preencha todos os campos"
                        return@Button
                    }

                    carregando = true
                    erro = null

                    scope.launch {
                        try {
                            auth.signInWithEmailAndPassword(email, senha).await()
                            onLoginSucesso()
                        } catch (e: Exception) {
                            erro = when (e) {
                                is FirebaseAuthInvalidUserException -> "Usuário não encontrado"
                                is FirebaseAuthInvalidCredentialsException -> "Senha incorreta"
                                else -> "Erro ao fazer login: ${e.localizedMessage}"
                            }
                        } finally {
                            carregando = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !carregando
            ) {
                if (carregando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF2196F3),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Entrar", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.5f))
                Text(" ou ", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Criar conta",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    onCriarConta()
                }
            )
        }
    }
}