package com.tazy.meuapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CabecalhoUsuario(state: TelaPrincipalState, navController: NavController) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Olá, ${state.nomeUsuario}!",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Text(
                    text = "Condomínio: ${state.codigoCondominio}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        },
        actions = {
            TextButton(
                onClick = {
                    navController.navigate("loginPrimeiraVez/editar") {
                        popUpTo("lobbyPrincipal") { saveState = true }
                    }
                }
            ) {
                Text("Editar Perfil", color = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2196F3))
    )
}


