package com.tazy.meuapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
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

// ─── INTERESSES ─────────────────────────────────────
data class Interesse(
    val id: String,
    val label: String,
    val emoji: String
)

val listaInteresses = listOf(
    Interesse("jogos","Jogos","🎮"),
    Interesse("esportes","Esportes","⚽"),
    Interesse("musica","Música","🎵"),
    Interesse("cinema","Cinema","🎬"),
    Interesse("leitura","Leitura","📚"),
    Interesse("tecnologia","Tecnologia","💻"),
    Interesse("gastronomia","Gastronomia","🍕"),
    Interesse("academia","Academia","🏋️"),
    Interesse("viagens","Viagens","✈️"),
    Interesse("arte","Arte","🎨"),
    Interesse("animais","Animais","🐾"),
    Interesse("natureza","Natureza","🌿"),
)

@Composable
fun CadastroScreen(
    onCadastroFinalizado: () -> Unit,
    onCadastroPrimeiroLogin: () -> Unit
) {

    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var interesseSelecionado by remember { mutableStateOf<String?>(null) }

    var erro by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    val scroll = rememberScrollState()

    val botaoAtivo =
        nome.isNotBlank() &&
                email.isNotBlank() &&
                senha.length >= 6 &&
                interesseSelecionado != null

    val infiniteTransition = rememberInfiniteTransition()

    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(3500, easing = EaseInOutSine),
            RepeatMode.Reverse
        )
    )

    val gradientButton = Brush.linearGradient(
        listOf(AccentPurple, AccentBlue, AccentCyan)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BgDeep, BgMid, Color(0xFF050A0F))
                )
            )
    ) {

        Canvas(Modifier.fillMaxSize()) {

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        AccentCyan.copy(alpha = 0.12f * orbPulse),
                        Color.Transparent
                    )
                ),
                center = Offset(size.width * 0.15f, size.height * 0.1f),
                radius = size.width * 0.6f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        AccentPurple.copy(alpha = 0.15f * orbPulse),
                        Color.Transparent
                    )
                ),
                center = Offset(size.width * 0.9f, size.height * 0.6f),
                radius = size.width * 0.55f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(30.dp))

            Text(
                "Criar Conta",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Text(
                "Preencha seus dados para começar",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
            )

            KlancoreTextField(
                value = nome,
                onValueChange = { nome = it },
                placeholder = "Nome completo",
                icon = Icons.Default.Person
            )

            Spacer(Modifier.height(12.dp))

            KlancoreTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "E-mail",
                icon = Icons.Default.Email
            )

            Spacer(Modifier.height(12.dp))

            KlancoreTextField(
                value = senha,
                onValueChange = { senha = it },
                placeholder = "Senha",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(AccentCyan, AccentPurple)
                            ),
                            RoundedCornerShape(2.dp)
                        )
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    "Seu principal interesse",
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Text(
                "Você verá outros usuários com o mesmo interesse",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            listaInteresses.chunked(3).forEach { rowItems ->

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    rowItems.forEach { interesse ->

                        val selecionado = interesseSelecionado == interesse.id

                        val scale by animateFloatAsState(
                            if (selecionado) 1.05f else 1f
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .scale(scale)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selecionado)
                                        Brush.linearGradient(
                                            listOf(
                                                AccentCyan.copy(alpha = 0.15f),
                                                AccentPurple.copy(alpha = 0.15f)
                                            )
                                        )
                                    else Brush.linearGradient(listOf(FieldBg, FieldBg))
                                )
                                .border(
                                    width = if (selecionado) 1.5.dp else 1.dp,
                                    brush = if (selecionado)
                                        Brush.linearGradient(
                                            listOf(AccentCyan, AccentPurple)
                                        )
                                    else Brush.linearGradient(
                                        listOf(FieldBorder, FieldBorder)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    interesseSelecionado = interesse.id
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Text(interesse.emoji, fontSize = 20.sp)

                                Spacer(Modifier.height(3.dp))

                                Text(
                                    interesse.label,
                                    fontSize = 11.sp,
                                    fontWeight =
                                        if (selecionado)
                                            FontWeight.Bold
                                        else
                                            FontWeight.Normal,
                                    color =
                                        if (selecionado)
                                            AccentCyan
                                        else
                                            TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            erro?.let {

                Text(
                    it,
                    color = Color.Red,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(6.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        if (botaoAtivo)
                            gradientButton
                        else
                            Brush.linearGradient(
                                listOf(Color.Gray, Color.Gray)
                            )
                    )
                    .clickable(enabled = botaoAtivo) {

                        if (!android.util.Patterns.EMAIL_ADDRESS
                                .matcher(email)
                                .matches()
                        ) {
                            erro = "Email inválido"
                            return@clickable
                        }

                        carregando = true
                        erro = null

                        scope.launch {

                            try {

                                val result =
                                    auth.createUserWithEmailAndPassword(
                                        email,
                                        senha
                                    ).await()

                                val uid =
                                    result.user?.uid ?: return@launch

                                val usuario = hashMapOf(
                                    "nome" to nome,
                                    "email" to email,
                                    "interesse" to interesseSelecionado
                                )

                                firestore.collection("usuarios")
                                    .document(uid)
                                    .set(usuario, SetOptions.merge())
                                    .await()

                                onCadastroPrimeiroLogin()

                            } catch (e: Exception) {

                                erro =
                                    "Erro ao criar conta. Tente novamente."

                            }

                            carregando = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {

                if (carregando) {

                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )

                } else {

                    Text(
                        "Cadastrar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "Voltar para login",
                color = TextSecondary,
                modifier = Modifier.clickable {
                    onCadastroFinalizado()
                }
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}