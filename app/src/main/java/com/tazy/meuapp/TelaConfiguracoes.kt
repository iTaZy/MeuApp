package com.tazy.meuapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaConfiguracoes(navController: NavController) {
    // Estados para o popup de exclusão
    var mostrarDialogExclusao by remember { mutableStateOf(false) }
    var apagandoConta by remember { mutableStateOf(false) }
    var erroMensagem by remember { mutableStateOf("") }

    // Paleta KLANCORE
    val BgDeep = Color(0xFF060B10)
    val BgMid = Color(0xFF0B1422)
    val AccentCyan = Color(0xFF4DD9E8)
    val TextPrimary = Color(0xFFE8F4FF)
    val TextSecondary = Color(0xFF8BA8C0)
    val VermelhoAlerta = Color(0xFFFF4B4B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgMid, Color(0xFF050A0F))))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text("Configurações", color = TextPrimary, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = AccentCyan)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // SEÇÃO: CONTA
                Text(
                    text = "CONTA",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
                ConfigItem(
                    icon = Icons.Default.Lock,
                    title = "Privacidade e Segurança",
                    onClick = { /* Para o futuro */ }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // SEÇÃO: PREFERÊNCIAS
                Text(
                    text = "PREFERÊNCIAS",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
                ConfigItem(
                    icon = Icons.Default.Notifications,
                    title = "Notificações",
                    onClick = { /* Para o futuro */ }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // SEÇÃO: SOBRE
                Text(
                    text = "SOBRE",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
                ConfigItem(
                    icon = Icons.Default.Info,
                    title = "Sobre o Klancore",
                    onClick = { /* Para o futuro */ }
                )

                Spacer(modifier = Modifier.weight(1f)) // Empurra os botões pro final

                // ✨ NOVO BOTÃO: EXCLUIR CONTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.Transparent)
                        .border(1.5.dp, VermelhoAlerta.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                        .clickable { mostrarDialogExclusao = true }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = VermelhoAlerta
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Excluir Conta",
                            color = VermelhoAlerta,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                // BOTÃO: SAIR DA CONTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                        .navigationBarsPadding() // Protege contra os botões nativos do Android
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF1A0A10)) // Fundo vermelho escuro
                        .border(1.5.dp, VermelhoAlerta, RoundedCornerShape(32.dp))
                        .clickable {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sair",
                            tint = VermelhoAlerta
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sair da Conta",
                            color = VermelhoAlerta,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // ✨ CAIXA DE CONFIRMAÇÃO (DIALOG)
        if (mostrarDialogExclusao) {
            AlertDialog(
                onDismissRequest = { if (!apagandoConta) mostrarDialogExclusao = false },
                containerColor = BgMid,
                title = {
                    Text("Você tem certeza?", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            "Essa ação é irreversível. Todas as suas fotos, conexões e mensagens serão apagadas para sempre.",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        if (erroMensagem.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(erroMensagem, color = VermelhoAlerta, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            apagandoConta = true
                            erroMensagem = ""
                            val auth = FirebaseAuth.getInstance()
                            val firestore = FirebaseFirestore.getInstance()
                            val user = auth.currentUser

                            if (user != null) {
                                // 1. Apaga os dados do banco
                                firestore.collection("usuarios").document(user.uid).delete()
                                    .addOnSuccessListener {
                                        // 2. Apaga a conta do sistema de login
                                        user.delete()
                                            .addOnSuccessListener {
                                                navController.navigate("login") {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener {
                                                apagandoConta = false
                                                erroMensagem = "Erro: Faça logout, entre novamente e tente excluir."
                                            }
                                    }
                            }
                        },
                        enabled = !apagandoConta
                    ) {
                        if (apagandoConta) {
                            CircularProgressIndicator(color = VermelhoAlerta, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Sim, excluir", color = VermelhoAlerta, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { mostrarDialogExclusao = false },
                        enabled = !apagandoConta
                    ) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun ConfigItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D1A2A).copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4DD9E8).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF4DD9E8), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, color = Color(0xFFE8F4FF), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8BA8C0))
    }
    Spacer(modifier = Modifier.height(8.dp))
}