package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaLobbyPrincipal(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lobby Principal", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2196F3))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Bem-vindo!", fontSize = 24.sp, color = Color(0xFF1976D2))
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.navigate("telaPrincipalGrupos") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Group, contentDescription = "Grupos")
                    Spacer(Modifier.width(8.dp))
                    Text("Entrar em Grupos")
                }

                // Adicione mais funcionalidades aqui futuramente
            }
        }
    }
}
