// MatchNotification.kt
package com.tazy.meuapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tazy.meuapp.model.Profile

@Composable
fun MatchNotification(
    isVisible: Boolean,
    currentUserName: String,
    matchedProfile: Profile,
    onDismiss: () -> Unit
) {
    val azulPrimario = Color(0xFF2196F3)
    val rosaMatch = Color(0xFFE91E63)

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            // Animação de entrada
            val scale by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ), label = "scale"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ícone de coração
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Match",
                        modifier = Modifier.size(64.dp),
                        tint = rosaMatch
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Título
                    Text(
                        text = "É um Match! 🎉",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = rosaMatch,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Descrição
                    Text(
                        text = "Você e ${matchedProfile.name} se curtiram mutuamente!",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botões
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botão Continuar Navegando
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = azulPrimario
                            )
                        ) {
                            Text("Continuar")
                        }

                        // Botão Enviar Mensagem (funcionalidade futura)
                        Button(
                            onClick = {
                                // Navegar para chat ou funcionalidade futura
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = rosaMatch)
                        ) {
                            Text("Mensagem", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}