package com.tazy.meuapp.model

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tazy.meuapp.R

@Composable
fun RodapeUsuario(navController: NavController, selected: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Linha azul clara no topo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFB3E5FC)) // Azul claro
        )

        // Rodapé com os botões
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 12.dp, bottom = 16.dp),
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
        }
    }
}

@Composable
fun BotaoRodapePersonalizado(iconRes: Int, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF1976D2) else Color(0xFF2196F3)
    val textColor = if (isSelected) Color(0xFF1976D2) else Color.Gray

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .offset(y = (-6).dp)
                .background(color = backgroundColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp)) // Aproxima o texto do botão
        Text(
            text = label,
            fontSize = 12.sp,
            color = textColor
        )
    }
}

