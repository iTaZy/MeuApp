package com.tazy.meuapp.model

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tazy.meuapp.TelaPrincipalState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CabecalhoUsuario(state: TelaPrincipalState, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Olá, ${state.nomeUsuario}!",
                fontSize = 24.sp,
                color = Color.Black
            )
            Text(
                text = "Condominio: ${state.codigoCondominio}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        OutlinedButton(
            onClick = {
                navController.navigate("loginPrimeiraVez/editar") {
                    popUpTo("lobbyPrincipal") { saveState = true }
                }
            },
            border = BorderStroke(1.dp, Color(0xFF2196F3)), // Borda azul clara
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            ),
            modifier = Modifier.height(36.dp) // Altura parecida com a da imagem
        ) {
            Text("Editar Perfil", fontSize = 14.sp)
        }
    }
}
