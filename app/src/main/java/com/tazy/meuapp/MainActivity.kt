package com.tazy.meuapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
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

// ─── Paleta KLANCORE ───────────────────────────────────────────────────────
private val BgDeep        = Color(0xFF060B10)
private val BgMid         = Color(0xFF0B1422)
private val AccentCyan    = Color(0xFF4DD9E8)
private val AccentPurple  = Color(0xFF8B5CF6)
private val AccentBlue    = Color(0xFF3B82F6)
private val FieldBorder   = Color(0xFFFFFFFF).copy(alpha = 0.18f)
private val FieldFocused  = Color(0xFFFFFFFF).copy(alpha = 0.45f)
private val FieldBg       = Color(0xFF0D1A2A).copy(alpha = 0.75f)
private val TextPrimary   = Color(0xFFE8F4FF)
private val TextSecondary = Color(0xFF8BA8C0)
private val ButtonBlue    = Color(0xFF4A8FE0)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MeuAppTheme { NavGraph() } }
    }
}

@Composable
fun LoginScreen(
    onCriarConta: () -> Unit,
    onLoginSucesso: () -> Unit
) {
    var email      by remember { mutableStateOf("") }
    var senha      by remember { mutableStateOf("") }
    var erro       by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

    val auth  = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    // ── Animações ──────────────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(3500, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "orb"
    )

    // Logo: flutuação vertical suave
    val logoFloat by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(
            tween(2800, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "float"
    )

    // Halo da logo pulsante
    val logoGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "glow"
    )

    // Animação de entrada (fade + slide)
    var visible by remember { mutableStateOf(false) }
    val enterAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "alpha"
    )
    val enterSlide by animateFloatAsState(
        targetValue = if (visible) 0f else 50f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "slide"
    )
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgMid, Color(0xFF050A0F))))
    ) {

        // ── Orbs de luz no background ──────────────────────────────────────
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(AccentCyan.copy(alpha = 0.13f * orbPulse), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.12f),
                    radius = size.width * 0.6f
                ),
                center = Offset(size.width * 0.1f, size.height * 0.12f),
                radius = size.width * 0.6f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(AccentPurple.copy(alpha = 0.16f * orbPulse), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.45f),
                    radius = size.width * 0.55f
                ),
                center = Offset(size.width * 0.9f, size.height * 0.45f),
                radius = size.width * 0.55f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(AccentBlue.copy(alpha = 0.10f * orbPulse), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.85f),
                    radius = size.width * 0.5f
                ),
                center = Offset(size.width * 0.15f, size.height * 0.85f),
                radius = size.width * 0.5f
            )
        }

        // ── Conteúdo principal ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .graphicsLayer {
                    alpha        = enterAlpha
                    translationY = enterSlide
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Logo maior com halo pulsante e flutuação ───────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(y = logoFloat.dp)
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                // Halo externo
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                AccentPurple.copy(alpha = 0.28f * logoGlow),
                                AccentCyan.copy(alpha = 0.10f * logoGlow),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension * 0.52f
                        ),
                        radius = size.minDimension * 0.52f
                    )
                }
                Image(
                    painter            = painterResource(id = R.drawable.logo_klancore),
                    contentDescription = "KLANCORE Logo",
                    modifier           = Modifier.size(500.dp)
                )
            }

            // ── Wordmark ───────────────────────────────────────────────────
            Row(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    "KLAN",
                    fontSize      = 32.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = AccentPurple,
                    letterSpacing = 3.sp
                )
                Text(
                    "CORE",
                    fontSize      = 32.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = TextPrimary,
                    letterSpacing = 3.sp
                )
            }

            // ── Campos ─────────────────────────────────────────────────────
            KlancoreTextField(
                value = email, onValueChange = { email = it }, placeholder = "E-mail"
            )
            Spacer(Modifier.height(12.dp))
            KlancoreTextField(
                value = senha, onValueChange = { senha = it },
                placeholder = "Senha", isPassword = true
            )

            Text(
                "Esqueci minha senha",
                color    = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 10.dp, bottom = 28.dp)
                    .clickable { /* TODO */ }
            )

            // ── Erro com card sutil ────────────────────────────────────────
            erro?.let {
                Surface(
                    shape    = RoundedCornerShape(8.dp),
                    color    = Color(0xFFFF4444).copy(alpha = 0.10f),
                    border   = BorderStroke(1.dp, Color(0xFFFF4444).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                ) {
                    Text(
                        it, color = Color(0xFFFF7070), fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            // ── Botão Entrar ───────────────────────────────────────────────
            Button(
                onClick = {
                    if (email.isBlank() || senha.isBlank()) {
                        erro = "Preencha todos os campos"; return@Button
                    }
                    carregando = true; erro = null
                    scope.launch {
                        try {
                            auth.signInWithEmailAndPassword(email, senha).await()
                            onLoginSucesso()
                        } catch (e: Exception) {
                            erro = when (e) {
                                is FirebaseAuthInvalidUserException        -> "Usuário não encontrado"
                                is FirebaseAuthInvalidCredentialsException -> "Senha incorreta"
                                else -> "Erro: ${e.localizedMessage}"
                            }
                        } finally { carregando = false }
                    }
                },
                colors   = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape    = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = !carregando,
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp)
            ) {
                if (carregando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp), color = AccentBlue, strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Entrar", color = ButtonBlue,
                        fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Divisor ────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(Modifier.weight(1f), color = FieldBorder)
                Text("  ou  ", color = TextSecondary, fontSize = 12.sp)
                HorizontalDivider(Modifier.weight(1f), color = FieldBorder)
            }

            Spacer(Modifier.height(28.dp))

            // ── Criar conta ────────────────────────────────────────────────
            Text(
                "Criar conta",
                color      = TextPrimary,
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.clickable { onCriarConta() }.padding(bottom = 40.dp)
            )
        }
    }
}

// ── Campo de texto estilo KLANCORE ─────────────────────────────────────────
@Composable
fun KlancoreTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        placeholder   = { Text(placeholder, color = TextSecondary, fontSize = 15.sp) },
        visualTransformation = if (isPassword) PasswordVisualTransformation()
        else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FieldBg),
        shape  = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = FieldFocused,
            unfocusedBorderColor    = FieldBorder,
            focusedTextColor        = TextPrimary,
            unfocusedTextColor      = TextPrimary,
            cursorColor             = AccentCyan,
            focusedContainerColor   = FieldBg,
            unfocusedContainerColor = FieldBg
        ),
        singleLine = true
    )
}