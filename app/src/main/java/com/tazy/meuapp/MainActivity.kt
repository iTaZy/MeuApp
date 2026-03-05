package com.tazy.meuapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.tazy.meuapp.ui.theme.MeuAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

// ─── CORES ─────────────────────────────────────

val BgDeep = Color(0xFF060B10)
val BgMid = Color(0xFF0B1422)

val AccentCyan = Color(0xFF4DD9E8)
val AccentPurple = Color(0xFF8B5CF6)
val AccentBlue = Color(0xFF3B82F6)

val FieldBorder = Color(0xFFFFFFFF).copy(alpha = 0.18f)
private val FieldFocused = Color(0xFFFFFFFF).copy(alpha = 0.45f)
val FieldBg = Color(0xFF0D1A2A)

val TextPrimary = Color(0xFFE8F4FF)
val TextSecondary = Color(0xFF8BA8C0)

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

    val infiniteTransition = rememberInfiniteTransition()

    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(3500, easing = EaseInOutSine),
            RepeatMode.Reverse
        )
    )

    val logoFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = EaseInOutSine),
            RepeatMode.Reverse
        )
    )

    val gradientButton = Brush.linearGradient(
        listOf(
            AccentPurple,
            AccentBlue,
            AccentCyan
        )
    )

    val scale by animateFloatAsState(
        if (carregando) 0.95f else 1f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        BgDeep,
                        BgMid,
                        Color(0xFF050A0F)
                    )
                )
            )
    ) {

        // ───── ORBS DE FUNDO ─────

        Canvas(Modifier.fillMaxSize()) {

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        AccentCyan.copy(alpha = 0.15f * orbPulse),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.2f, size.height * 0.2f),
                    radius = size.width * 0.7f
                ),
                center = Offset(size.width * 0.2f, size.height * 0.2f),
                radius = size.width * 0.7f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        AccentPurple.copy(alpha = 0.18f * orbPulse),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.9f, size.height * 0.4f),
                    radius = size.width * 0.6f
                ),
                center = Offset(size.width * 0.9f, size.height * 0.4f),
                radius = size.width * 0.6f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // LOGO
            Image(
                painter = painterResource(id = R.drawable.logo_klancore),
                contentDescription = "Logo",
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // NOME (substitui o Row com os Text)
            Image(
                painter = painterResource(id = R.drawable.klancore_text),
                contentDescription = "Klancore",
                modifier = Modifier
                    .width(300.dp)
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ───── EMAIL ─────

            KlancoreTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "E-mail",
                icon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ───── SENHA ─────

            KlancoreTextField(
                value = senha,
                onValueChange = { senha = it },
                placeholder = "Senha",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            Text(
                "Esqueci minha senha",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 10.dp, bottom = 30.dp)
            )

            // ───── ERRO ─────

            erro?.let {

                Text(
                    it,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            // ───── BOTÃO LOGIN ─────

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(32.dp))
                    .background(gradientButton)
                    .clickable {

                        if (email.isBlank() || senha.isBlank()) {
                            erro = "Preencha todos os campos"
                            return@clickable
                        }

                        carregando = true
                        erro = null

                        scope.launch {

                            try {

                                auth.signInWithEmailAndPassword(email, senha).await()

                                onLoginSucesso()

                            } catch (e: Exception) {

                                erro = when (e) {
                                    is FirebaseAuthInvalidUserException ->
                                        "Usuário não encontrado"

                                    is FirebaseAuthInvalidCredentialsException ->
                                        "Senha incorreta"

                                    else -> "Erro: ${e.localizedMessage}"
                                }
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
                        "Entrar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = FieldBorder
                )

                Text(
                    "  ou  ",
                    color = TextSecondary
                )

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = FieldBorder
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row {

                Text(
                    "Não tem conta? ",
                    color = TextSecondary
                )

                Text(
                    "Criar conta",
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onCriarConta() }
                )
            }
        }
    }
}

// ───── TEXT FIELD ─────

@Composable
fun KlancoreTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {

    var senhaVisivel by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,

        leadingIcon = {
            Icon(icon, contentDescription = null, tint = TextSecondary)
        },

        trailingIcon = {

            if (isPassword) {

                val iconEye =
                    if (senhaVisivel)
                        Icons.Default.Visibility
                    else
                        Icons.Default.VisibilityOff

                Icon(
                    iconEye,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.clickable {
                        senhaVisivel = !senhaVisivel
                    }
                )
            }
        },

        placeholder = {
            Text(placeholder, color = TextSecondary)
        },

        visualTransformation =
            if (isPassword && !senhaVisivel)
                PasswordVisualTransformation()
            else
                VisualTransformation.None,

        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FieldBg),

        shape = RoundedCornerShape(12.dp),

        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FieldFocused,
            unfocusedBorderColor = FieldBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = AccentCyan,
            focusedContainerColor = FieldBg,
            unfocusedContainerColor = FieldBg
        ),

        singleLine = true
    )
}