package com.tazy.meuapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ItemConversa(
    nome: String,
    mensagem: String,
    horario: String,
    onClick: () -> Unit = {}
) {
    // Cores da paleta Klancore
    val AccentCyan = Color(0xFF4DD9E8)
    val AccentPurple = Color(0xFF8B5CF6)
    val TextPrimary = Color(0xFFE8F4FF)
    val TextSecondary = Color(0xFF8BA8C0)
    val FieldBg = Color(0xFF0D1A2A).copy(alpha = 0.75f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Foto de perfil com identidade Klancore (borda gradiente e fundo translúcido)
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(AccentCyan.copy(0.2f), FieldBg)))
                .border(2.dp, Brush.linearGradient(listOf(AccentCyan, AccentPurple)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Foto de perfil",
                tint = TextSecondary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Informações da conversa
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nome,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = horario,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // A mensagem agora é exibida corretamente
            Text(
                text = mensagem,
                fontSize = 14.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}