package com.meuapp.ui.telas

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tazy.meuapp.R
import com.tazy.meuapp.TelaPrincipalState
import com.tazy.meuapp.model.CabecalhoUsuario
import com.tazy.meuapp.model.RodapeUsuario
import com.tazy.meuapp.viewmodel.ConexoesUiState
import com.tazy.meuapp.viewmodel.ConexoesViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaConexoes(
    navController: NavController,
    viewModel: ConexoesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val matchState by viewModel.matchState.collectAsState() // 👇 NOVO: Escuta os matches!

    var nomeUsuario by remember { mutableStateOf("Usuário") }
    var carregandoDados by remember { mutableStateOf(true) }

    // Controlo do Dialog de Filtros
    var mostrarFiltro by remember { mutableStateOf(false) }

    // Paleta KLANCORE
    val BgDeep = Color(0xFF060B10)
    val BgMid = Color(0xFF0B1422)
    val AccentCyan = Color(0xFF4DD9E8)
    val AccentPurple = Color(0xFF8B5CF6)
    val AccentBlue = Color(0xFF3B82F6)
    val TextPrimary = Color(0xFFE8F4FF)
    val TextSecondary = Color(0xFF8BA8C0)
    val FieldBg = Color(0xFF0D1A2A).copy(alpha = 0.75f)

    val gradientButton = Brush.linearGradient(listOf(AccentPurple, AccentBlue, AccentCyan))
    val gradientBorder = Brush.linearGradient(listOf(AccentCyan, AccentPurple))

    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "orbPulse"
    )

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            firestore.collection("usuarios").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    nomeUsuario = document.getString("nome") ?: currentUser.displayName ?: "Usuário"
                    carregandoDados = false
                }
                .addOnFailureListener {
                    nomeUsuario = currentUser.displayName ?: "Usuário"
                    carregandoDados = false
                }
        } else {
            carregandoDados = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgMid, Color(0xFF050A0F))))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(AccentCyan.copy(alpha = 0.15f * orbPulse), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.2f),
                    radius = size.width * 0.5f
                ),
                center = Offset(size.width * 0.2f, size.height * 0.2f),
                radius = size.width * 0.5f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(AccentPurple.copy(alpha = 0.15f * orbPulse), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.6f),
                    radius = size.width * 0.6f
                ),
                center = Offset(size.width * 0.8f, size.height * 0.6f),
                radius = size.width * 0.6f
            )
        }

        // --- DIALOG DE FILTROS ---
        if (mostrarFiltro) {
            Dialog(onDismissRequest = { mostrarFiltro = false }) {
                // Estados locais do popup
                var faixaIdade by remember { mutableStateOf(viewModel.filtroIdadeMinima.toFloat()..viewModel.filtroIdadeMaxima.toFloat()) }
                var interesseSelecionado by remember { mutableStateOf(viewModel.filtroInteresseFocado) }
                var sexualidadeSelecionada by remember { mutableStateOf(viewModel.filtroSexualidade) }

                // Opções para os chips
                val meusInteresses by viewModel.meusInteresses.collectAsState()
                val opcoesSexualidade = listOf("Heterossexual", "Homossexual",  "Bissexual", "Pansexual")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.verticalGradient(listOf(BgMid, BgDeep)))
                        .border(1.5.dp, gradientBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Ajustar Radar",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 1. FILTRO DE IDADE
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Faixa Etária", color = TextSecondary, fontSize = 14.sp)
                            Text(
                                text = "${faixaIdade.start.roundToInt()} a ${faixaIdade.endInclusive.roundToInt()} anos",
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold, fontSize = 14.sp
                            )
                        }
                        RangeSlider(
                            value = faixaIdade,
                            onValueChange = { faixaIdade = it },
                            valueRange = 18f..100f,
                            colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentPurple, inactiveTrackColor = FieldBg)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. FILTRO DE INTERESSE FOCADO
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Focar Interesse em:", color = TextSecondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChipVisual(
                                        text = "Todos",
                                        isSelected = interesseSelecionado.isEmpty(),
                                        onClick = { interesseSelecionado = "" }
                                    )
                                }
                                items(meusInteresses) { interesse ->
                                    FilterChipVisual(
                                        text = interesse,
                                        isSelected = interesseSelecionado == interesse,
                                        onClick = { interesseSelecionado = interesse }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 3. FILTRO DE SEXUALIDADE
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Sexualidade:", color = TextSecondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChipVisual(
                                        text = "Qualquer",
                                        isSelected = sexualidadeSelecionada.isEmpty(),
                                        onClick = { sexualidadeSelecionada = "" }
                                    )
                                }
                                items(opcoesSexualidade) { sexo ->
                                    FilterChipVisual(
                                        text = sexo,
                                        isSelected = sexualidadeSelecionada.equals(sexo, ignoreCase = true),
                                        onClick = { sexualidadeSelecionada = sexo }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // BOTÕES DO DIALOG
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { mostrarFiltro = false }) {
                                Text("Cancelar", color = TextSecondary)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(gradientButton)
                                    .clickable {
                                        viewModel.aplicarFiltros(
                                            minIdade = faixaIdade.start.roundToInt(),
                                            maxIdade = faixaIdade.endInclusive.roundToInt(),
                                            interesse = interesseSelecionado,
                                            sexualidade = sexualidadeSelecionada
                                        )
                                        mostrarFiltro = false
                                    }
                                    .padding(horizontal = 24.dp, vertical = 10.dp)
                            ) {
                                Text("Procurar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            if (!carregandoDados) {
                CabecalhoUsuario(
                    state = TelaPrincipalState(nomeUsuario = nomeUsuario, codigoCondominio = ""),
                    navController = navController
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Título e Botão de Filtro
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Buscando Novas Conexões...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = { mostrarFiltro = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FieldBg)
                            .border(1.dp, AccentCyan.copy(0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Filtros", tint = AccentCyan, modifier = Modifier.size(20.dp))
                    }
                }

                when (val state = uiState) {
                    is ConexoesUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AccentCyan)
                        }
                    }

                    is ConexoesUiState.Error -> {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = state.message,
                                color = Color(0xFFFF5252),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    is ConexoesUiState.Empty -> {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Vazio",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
                                )
                                Text(
                                    text = "Nenhum perfil encontrado com os filtros atuais.",
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(32.dp))
                                        .background(gradientButton)
                                        .clickable { viewModel.reloadProfiles() }
                                        .padding(horizontal = 32.dp, vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Recarregar Radar", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    is ConexoesUiState.Success -> {
                        val profile = state.currentProfile
                        val currentIndex = state.currentIndex
                        val totalProfiles = state.totalProfiles

                        val nomeEIdade = "${profile.name}, ${profile.age}"
                        val displayIndex = currentIndex + 1
                        val progressoTexto = "Conexão $displayIndex de $totalProfiles"

                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(FieldBg)
                                    .border(1.5.dp, gradientBorder, RoundedCornerShape(24.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.radialGradient(listOf(AccentCyan.copy(0.1f), Color.Transparent))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Foto",
                                        modifier = Modifier.size(100.dp),
                                        tint = TextSecondary.copy(alpha = 0.3f)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.6f)
                                        .align(Alignment.BottomCenter)
                                        .background(Brush.verticalGradient(listOf(Color.Transparent, BgDeep.copy(0.8f), BgDeep)))
                                )

                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .padding(24.dp)
                                ) {
                                    Text(
                                        text = nomeEIdade,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )

                                    Row(
                                        modifier = Modifier.padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (profile.sexualidade.isNotEmpty()) {
                                            TagInfo(text = profile.sexualidade, color = AccentCyan)
                                        }
                                        if (profile.signo.isNotEmpty()) {
                                            TagInfo(text = profile.signo, color = AccentPurple)
                                        }
                                    }

                                    if (profile.bio.isNotEmpty()) {
                                        Text(
                                            text = profile.bio,
                                            fontSize = 14.sp,
                                            color = TextSecondary,
                                            lineHeight = 20.sp,
                                            modifier = Modifier.padding(top = 12.dp)
                                        )
                                    }

                                    if (profile.interesses.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            profile.interesses.take(3).forEach { interesse ->
                                                TagInfo(text = interesse, color = AccentBlue, isFilled = true)
                                            }
                                            if (profile.interesses.size > 3) {
                                                val extras = profile.interesses.size - 3
                                                val textoExtra = "+$extras"
                                                TagInfo(text = textoExtra, color = TextSecondary)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = progressoTexto,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButtonCustom(
                                    icon = R.drawable.ic_seta_voltar,
                                    enabled = viewModel.canGoBack(),
                                    onClick = { viewModel.previousProfile() },
                                    color = AccentCyan
                                )

                                Box(
                                    modifier = Modifier
                                        .height(56.dp)
                                        .weight(1f)
                                        .padding(horizontal = 16.dp)
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(gradientButton)
                                        .clickable { viewModel.likeProfile() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Criar Vínculo",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                IconButtonCustom(
                                    icon = R.drawable.ic_seta_avancar,
                                    enabled = viewModel.canGoForward(),
                                    onClick = { viewModel.nextProfile() },
                                    color = AccentPurple
                                )
                            }
                        }
                    }
                }
            }
            RodapeUsuario(navController = navController, selected = "Matches")
        }
        if (matchState is com.tazy.meuapp.viewmodel.MatchState.NewMatch) {
            val matchData = matchState as com.tazy.meuapp.viewmodel.MatchState.NewMatch

            com.tazy.meuapp.ui.components.MatchNotification(
                isVisible = true,
                currentUserName = nomeUsuario,
                matchedProfile = matchData.matchedProfile,
                onDismiss = { viewModel.clearMatchState() },
                onMessageClick = {
                    viewModel.clearMatchState() // Limpa o popup da tela
                    navController.navigate("chat/${matchData.matchId}") // Navega pro chat
                }
            )
        }
    }
}

// Componente visual para os botões arredondados (Chips) dentro do popup de filtros
@Composable
fun FilterChipVisual(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val AccentCyan = Color(0xFF4DD9E8)
    val FieldBg = Color(0xFF0D1A2A).copy(alpha = 0.75f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) AccentCyan.copy(0.2f) else FieldBg)
            .border(1.dp, if (isSelected) AccentCyan else Color.Gray.copy(0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (isSelected) AccentCyan else Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TagInfo(text: String, color: Color, isFilled: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFilled) color.copy(alpha = 0.2f) else Color.Transparent)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun IconButtonCustom(icon: Int, enabled: Boolean, onClick: () -> Unit, color: Color) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFF0D1A2A))
            .border(2.dp, if (enabled) color.copy(alpha = 0.5f) else Color.Gray.copy(0.2f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = icon),
            contentDescription = "Icone de acao",
            tint = if (enabled) color else Color.Gray.copy(0.5f),
            modifier = Modifier.size(24.dp)
        )
    }
}