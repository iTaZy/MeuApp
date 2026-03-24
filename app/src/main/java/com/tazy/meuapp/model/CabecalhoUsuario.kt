package com.tazy.meuapp.model

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tazy.meuapp.AccentCyan
import com.tazy.meuapp.AccentPurple
import com.tazy.meuapp.FieldBg
import com.tazy.meuapp.TelaPrincipalState
import com.tazy.meuapp.TextPrimary
import com.tazy.meuapp.TextSecondary

@Composable
fun CabecalhoUsuario(
    state: TelaPrincipalState,
    navController: NavController
) {

    val infiniteTransition = rememberInfiniteTransition(label = "headerAnim")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val gradientBorder = Brush.linearGradient(
        listOf(AccentCyan, AccentPurple)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ─── LADO ESQUERDO ───
        Row(verticalAlignment = Alignment.CenterVertically) {

            // FOTO COM GLOW
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(AccentCyan.copy(0.25f), FieldBg)
                        )
                    )
                    .border(
                        2.dp,
                        gradientBorder,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Removido o código do condomínio, deixamos apenas a saudação
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "Olá, ${state.nomeUsuario}!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // ─── BOTÃO EDITAR (VERSÃO PREMIUM) ───
        val scale by animateFloatAsState(
            targetValue = 1f,
            label = "btnScale"
        )

        Box(
            modifier = Modifier
                .scale(scale)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            AccentCyan.copy(alpha = 0.15f),
                            AccentPurple.copy(alpha = 0.15f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = gradientBorder,
                    shape = RoundedCornerShape(30.dp)
                )
                .clickable {
                    navController.navigate("loginPrimeiraVez/editar") {
                        popUpTo("lobbyPrincipal") { saveState = true }
                    }
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Editar",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentCyan
                )
            }
        }
    }
}