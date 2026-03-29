package com.tazy.meuapp.model

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth // IMPORT IMPORTANTE AQUI
import com.tazy.meuapp.R
import com.tazy.meuapp.AccentCyan
import com.tazy.meuapp.AccentPurple
import com.tazy.meuapp.BgDeep
import com.tazy.meuapp.BgMid

@Composable
fun RodapeUsuario(navController: NavController, selected: String) {
    // Pegamos o ID do usuário logado para poder abrir o próprio perfil dele
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 20.dp)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0B1422), BgDeep))
            )
    ) {
        // linha gradiente no topo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.horizontalGradient(
                        listOf(AccentCyan.copy(alpha = 0.6f), AccentPurple.copy(alpha = 0.6f))
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgMid) // ou a cor que você estiver usando
                .navigationBarsPadding() // ✨ NOVO: Cria um escudo contra os botões nativos do celular!
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            BotaoRodapePersonalizado(
                iconRes = R.drawable.ic_lobby,
                label = "Lobby",
                isSelected = selected == "Lobby",
                onClick = { navController.navigate("LobbyPrincipal") }
            )
            BotaoRodapePersonalizado(
                iconRes = R.drawable.ic_grupos,
                label = "Grupos",
                isSelected = selected == "Grupos",
                onClick = { navController.navigate("TelaPrincipalGrupos") }
            )
            BotaoRodapePersonalizado(
                iconRes = R.drawable.ic_feed,
                label = "Feed",
                isSelected = selected == "Feed",
                onClick = { navController.navigate("TelaFeedNoticias") }
            )
            BotaoRodapePersonalizado(
                iconRes = R.drawable.ic_conexoes,
                label = "Conexões",
                isSelected = selected == "Conexões",
                onClick = { navController.navigate("TelaConexoes") }
            )
            // 👇 NOVO BOTÃO DE PERFIL
            BotaoRodapePersonalizado(
                iconRes = R.drawable.ic_perfil, // Certifique-se de ter um ícone com este nome ou troque para o que você já tem
                label = "Perfil",
                isSelected = selected == "Perfil",
                onClick = {
                    // Ao clicar, navegamos para a rota de perfil passando o NOSSO próprio ID!
                    if (currentUserId.isNotEmpty()) {
                        navController.navigate("perfil/$currentUserId")
                    }
                }
            )
        }
    }
}

@Composable
fun BotaoRodapePersonalizado(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected)
                        Brush.linearGradient(listOf(AccentCyan, AccentPurple))
                    else
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.05f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AccentCyan else Color.White.copy(alpha = 0.4f)
        )
    }
}