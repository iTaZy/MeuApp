package com.tazy.meuapp

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tazy.meuapp.model.RodapeUsuario

// ─── SUBCATEGORIAS ─────────────────────────────────────────────────────

data class Subcategoria(
    val id: String,
    val label: String,
    val emoji: String
)

val subcategoriasPorInteresse: Map<String, List<Subcategoria>> = mapOf(
    "jogos" to listOf(
        Subcategoria("lol",       "LoL",        "⚔️"),
        Subcategoria("fortnite",  "Fortnite",   "🏗️"),
        Subcategoria("valorant",  "Valorant",   "🎯"),
        Subcategoria("freefire",  "Free Fire",  "🔥"),
        Subcategoria("minecraft", "Minecraft",  "⛏️"),
        Subcategoria("cs2",       "CS2",        "💣"),
        Subcategoria("fifa",      "FC 25",      "⚽"),
        Subcategoria("gta",       "GTA",        "🚗"),
        Subcategoria("rpg",       "RPG",        "🐉"),
        Subcategoria("outros",    "Outros",     "🕹️"),
    ),
    "esportes" to listOf(
        Subcategoria("futebol",  "Futebol",  "⚽"),
        Subcategoria("basquete", "Basquete", "🏀"),
        Subcategoria("volei",    "Vôlei",    "🏐"),
        Subcategoria("natacao",  "Natação",  "🏊"),
        Subcategoria("corrida",  "Corrida",  "🏃"),
        Subcategoria("ciclismo", "Ciclismo", "🚴"),
        Subcategoria("lutas",    "Lutas",    "🥊"),
        Subcategoria("tenis",    "Tênis",    "🎾"),
        Subcategoria("crossfit", "Crossfit", "💪"),
        Subcategoria("outros",   "Outros",   "🏅"),
    ),
    "musica" to listOf(
        Subcategoria("funk",      "Funk",       "🎤"),
        Subcategoria("rap",       "Rap/Hip-hop","🎧"),
        Subcategoria("sertanejo", "Sertanejo",  "🤠"),
        Subcategoria("rock",      "Rock",       "🎸"),
        Subcategoria("pop",       "Pop",        "🌟"),
        Subcategoria("eletronica","Eletrônica", "🎛️"),
        Subcategoria("kpop",      "K-pop",      "🇰🇷"),
        Subcategoria("classica",  "Clássica",   "🎻"),
        Subcategoria("mpb",       "MPB",        "🎵"),
        Subcategoria("outros",    "Outros",     "🎶"),
    ),
    "cinema" to listOf(
        Subcategoria("acao",    "Ação",        "💥"),
        Subcategoria("terror",  "Terror",      "👻"),
        Subcategoria("romance", "Romance",     "💕"),
        Subcategoria("comedia", "Comédia",     "😂"),
        Subcategoria("anime",   "Anime",       "🌸"),
        Subcategoria("sci_fi",  "Sci-Fi",      "🚀"),
        Subcategoria("doc",     "Documentário","🎥"),
        Subcategoria("hq",      "Super-herói", "🦸"),
        Subcategoria("serie",   "Séries",      "📺"),
        Subcategoria("outros",  "Outros",      "🎬"),
    ),
    "leitura" to listOf(
        Subcategoria("ficcao",    "Ficção",    "🌌"),
        Subcategoria("romance",   "Romance",   "💕"),
        Subcategoria("auto_ajuda","Auto-ajuda","🧠"),
        Subcategoria("manga",     "Mangá",     "🌸"),
        Subcategoria("hq",        "HQ/Comics", "💥"),
        Subcategoria("terror",    "Terror",    "👻"),
        Subcategoria("policial",  "Policial",  "🔍"),
        Subcategoria("biografia", "Biografia", "📖"),
        Subcategoria("fantasia",  "Fantasia",  "🐉"),
        Subcategoria("outros",    "Outros",    "📚"),
    ),
    "tecnologia" to listOf(
        Subcategoria("programacao","Programação","👨‍💻"),
        Subcategoria("ia",         "IA",         "🤖"),
        Subcategoria("games_dev",  "Dev Games",  "🎮"),
        Subcategoria("hardware",   "Hardware",   "🔧"),
        Subcategoria("cyber",      "Cybersec",   "🔐"),
        Subcategoria("mobile",     "Mobile",     "📱"),
        Subcategoria("design",     "UI/UX",      "🎨"),
        Subcategoria("crypto",     "Crypto/Web3","₿"),
        Subcategoria("redes",      "Redes",      "🌐"),
        Subcategoria("outros",     "Outros",     "💡"),
    ),
    "gastronomia" to listOf(
        Subcategoria("churrasco",  "Churrasco",  "🥩"),
        Subcategoria("doces",      "Doces",      "🍰"),
        Subcategoria("saudavel",   "Saudável",   "🥗"),
        Subcategoria("italiana",   "Italiana",   "🍝"),
        Subcategoria("japonesa",   "Japonesa",   "🍣"),
        Subcategoria("brasileira", "Brasileira", "🇧🇷"),
        Subcategoria("fast_food",  "Fast Food",  "🍔"),
        Subcategoria("vegana",     "Vegana",     "🌱"),
        Subcategoria("drinks",     "Drinks/Bar", "🍹"),
        Subcategoria("outros",     "Outros",     "🍴"),
    ),
    "academia" to listOf(
        Subcategoria("musculacao","Musculação",    "💪"),
        Subcategoria("crossfit",  "Crossfit",      "🏋️"),
        Subcategoria("yoga",      "Yoga",          "🧘"),
        Subcategoria("pilates",   "Pilates",       "🤸"),
        Subcategoria("corrida",   "Corrida",       "🏃"),
        Subcategoria("funcional", "Funcional",     "⚡"),
        Subcategoria("spinning",  "Spinning",      "🚴"),
        Subcategoria("natacao",   "Natação",       "🏊"),
        Subcategoria("artes_m",   "Artes Marciais","🥋"),
        Subcategoria("outros",    "Outros",        "🏅"),
    ),
    "viagens" to listOf(
        Subcategoria("praias",    "Praias",      "🏖️"),
        Subcategoria("montanhas", "Montanhas",   "⛰️"),
        Subcategoria("europa",    "Europa",      "🏰"),
        Subcategoria("asia",      "Ásia",        "🏯"),
        Subcategoria("americas",  "Américas",    "🌎"),
        Subcategoria("mochilao",  "Mochilão",    "🎒"),
        Subcategoria("luxo",      "Viagem Luxo", "✈️"),
        Subcategoria("road_trip", "Road Trip",   "🚗"),
        Subcategoria("nacional",  "Brasil",      "🇧🇷"),
        Subcategoria("outros",    "Outros",      "🗺️"),
    ),
    "arte" to listOf(
        Subcategoria("desenho",    "Desenho",     "✏️"),
        Subcategoria("pintura",    "Pintura",     "🖌️"),
        Subcategoria("digital",    "Arte Digital","🖥️"),
        Subcategoria("fotografia", "Fotografia",  "📷"),
        Subcategoria("escultura",  "Escultura",   "🗿"),
        Subcategoria("graffiti",   "Graffiti",    "🎨"),
        Subcategoria("tatuagem",   "Tatuagem",    "💉"),
        Subcategoria("moda",       "Moda",        "👗"),
        Subcategoria("danca",      "Dança",       "💃"),
        Subcategoria("outros",     "Outros",      "🎭"),
    ),
    "animais" to listOf(
        Subcategoria("caes",     "Cachorros",  "🐕"),
        Subcategoria("gatos",    "Gatos",      "🐈"),
        Subcategoria("aves",     "Aves",       "🦜"),
        Subcategoria("peixes",   "Peixes",     "🐠"),
        Subcategoria("repteis",  "Répteis",    "🦎"),
        Subcategoria("roedores", "Roedores",   "🐹"),
        Subcategoria("exoticos", "Exóticos",   "🦋"),
        Subcategoria("fazenda",  "Fazenda",    "🐄"),
        Subcategoria("fauna",    "Fauna Livre","🦁"),
        Subcategoria("outros",   "Outros",     "🐾"),
    ),
    "natureza" to listOf(
        Subcategoria("trilhas",    "Trilhas",    "🥾"),
        Subcategoria("camping",    "Camping",    "⛺"),
        Subcategoria("surf",       "Surf",       "🏄"),
        Subcategoria("escalada",   "Escalada",   "🧗"),
        Subcategoria("mergulho",   "Mergulho",   "🤿"),
        Subcategoria("jardinagem", "Jardinagem", "🌱"),
        Subcategoria("astronomia", "Astronomia", "🔭"),
        Subcategoria("ecologia",   "Ecologia",   "♻️"),
        Subcategoria("cachoeiras", "Cachoeiras", "💧"),
        Subcategoria("outros",     "Outros",     "🌿"),
    ),
)

// ───────────────────────────────────────────────────────────────────────

@Composable
fun LoginPrimeiraVez(
    navController: NavController,
    modoEdicao: Boolean = false
) {
    val firestore   = FirebaseFirestore.getInstance()
    val auth        = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var nome                    by remember { mutableStateOf("") }
    var email                   by remember { mutableStateOf("") }
    var bio                     by remember { mutableStateOf("") }
    var idade                   by remember { mutableStateOf("") }
    var sexualidade             by remember { mutableStateOf("") }
    var signo                   by remember { mutableStateOf("") }
    var interesseAtual          by remember { mutableStateOf<String?>(null) }
    val subcategoriasSelecionadas = remember { mutableStateListOf<String>() }
    val outrosInteressesSelecionados = remember { mutableStateListOf<String>() }
    var erro                    by remember { mutableStateOf("") }
    var carregando              by remember { mutableStateOf(true) }
    var mostrarSeletorInteresse by remember { mutableStateOf(false) }

    val subcategorias = interesseAtual?.let { subcategoriasPorInteresse[it] } ?: emptyList()
    val botaoAtivo    = nome.isNotBlank() && idade.isNotBlank() && subcategoriasSelecionadas.isNotEmpty()

    // ── carrega dados ──
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            firestore.collection("usuarios").document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    nome        = doc.getString("nome")  ?: currentUser.displayName ?: ""
                    email       = doc.getString("email") ?: currentUser.email ?: ""
                    bio         = doc.getString("bio")   ?: ""
                    idade       = doc.get("idade")?.toString() ?: ""
                    sexualidade = doc.getString("sexualidade") ?: ""
                    signo       = doc.getString("signo") ?: ""
                    interesseAtual = doc.getString("interesse")

                    // suporte a lista ou string única legada
                    val subs = doc.get("subcategorias")
                    when (subs) {
                        is List<*> -> subcategoriasSelecionadas.addAll(subs.filterIsInstance<String>())
                        is String  -> if (subs.isNotBlank()) subcategoriasSelecionadas.add(subs)
                    }// ADICIONA ESTE BLOCO:
                    val outros = doc.get("outrosInteresses")
                    if (outros is List<*>) {
                        outrosInteressesSelecionados.addAll(outros.filterIsInstance<String>())
                    }
                    carregando = false
                }
                .addOnFailureListener {
                    email = currentUser.email ?: ""
                    carregando = false
                }
        } else { carregando = false }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "orbPulse"
    )
    val gradientButton = Brush.linearGradient(listOf(AccentPurple, AccentBlue, AccentCyan))

    // ── Dialog seletor de interesse ──
    if (mostrarSeletorInteresse) {
        Dialog(onDismissRequest = { mostrarSeletorInteresse = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.verticalGradient(listOf(BgMid, BgDeep)))
                    .border(1.dp, Brush.linearGradient(listOf(AccentCyan, AccentPurple)), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        "Trocar interesse principal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    listaInteresses.chunked(3).forEach { rowItems ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { interesse ->
                                val sel = interesseAtual == interesse.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (sel)
                                                Brush.linearGradient(listOf(AccentCyan.copy(0.2f), AccentPurple.copy(0.2f)))
                                            else Brush.linearGradient(listOf(FieldBg, FieldBg))
                                        )
                                        .border(
                                            width = if (sel) 1.5.dp else 1.dp,
                                            brush = if (sel)
                                                Brush.linearGradient(listOf(AccentCyan, AccentPurple))
                                            else Brush.linearGradient(listOf(FieldBorder, FieldBorder)),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            if (interesseAtual != interesse.id) {
                                                interesseAtual = interesse.id
                                                subcategoriasSelecionadas.clear()
                                                outrosInteressesSelecionados.remove(interesse.id)
                                            }
                                            mostrarSeletorInteresse = false
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(interesse.emoji, fontSize = 18.sp)
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            interesse.label,
                                            fontSize = 10.sp,
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sel) AccentCyan else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { mostrarSeletorInteresse = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            }
        }
    }

    // ── tela principal ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgMid, Color(0xFF050A0F))))
    ) {

        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(AccentCyan.copy(alpha = 0.10f * orbPulse), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.05f),
                    radius = size.width * 0.65f
                ),
                center = Offset(size.width * 0.1f, size.height * 0.05f),
                radius = size.width * 0.65f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(AccentPurple.copy(alpha = 0.13f * orbPulse), Color.Transparent),
                    center = Offset(size.width * 0.95f, size.height * 0.7f),
                    radius = size.width * 0.55f
                ),
                center = Offset(size.width * 0.95f, size.height * 0.7f),
                radius = size.width * 0.55f
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(48.dp))

                if (carregando) {
                    CircularProgressIndicator(color = AccentCyan, modifier = Modifier.padding(32.dp))
                } else {

                    Text(
                        if (modoEdicao) "Editar Perfil" else "Complete seu Perfil",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        if (modoEdicao) "Atualize suas informações" else "Conte um pouco sobre você",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    // ── foto ──
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(AccentCyan.copy(0.2f), FieldBg)))
                            .border(2.dp, Brush.linearGradient(listOf(AccentCyan, AccentPurple)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp), tint = TextSecondary)
                    }

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(FieldBg)
                            .border(1.dp, Brush.linearGradient(listOf(AccentCyan, AccentPurple)), RoundedCornerShape(20.dp))
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Alterar foto de perfil", color = AccentCyan, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── dados pessoais ──
                    SectionLabel("Dados pessoais", AccentCyan, AccentPurple)
                    Spacer(Modifier.height(12.dp))

                    KlancoreTextField(value = nome, onValueChange = { nome = it }, placeholder = "Nome completo", icon = Icons.Default.Person)
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = email, onValueChange = {}, enabled = false,
                        placeholder = { Text("E-mail", color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = TextSecondary) },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(FieldBg),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = FieldBorder, disabledTextColor = TextSecondary,
                            disabledContainerColor = FieldBg, disabledLeadingIconColor = TextSecondary
                        ), singleLine = true
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = idade, onValueChange = { if (it.length <= 3) idade = it },
                            placeholder = { Text("Idade", color = TextSecondary) },
                            leadingIcon = { Icon(Icons.Default.Cake, null, tint = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(FieldBg),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FieldFocused, unfocusedBorderColor = FieldBorder,
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                                cursorColor = AccentCyan, focusedContainerColor = FieldBg, unfocusedContainerColor = FieldBg
                            ), singleLine = true
                        )
                        OutlinedTextField(
                            value = sexualidade, onValueChange = { sexualidade = it },
                            placeholder = { Text("Sexualidade", color = TextSecondary) },
                            leadingIcon = { Icon(Icons.Default.Favorite, null, tint = TextSecondary) },
                            modifier = Modifier.weight(1.4f).clip(RoundedCornerShape(12.dp)).background(FieldBg),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FieldFocused, unfocusedBorderColor = FieldBorder,
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                                cursorColor = AccentCyan, focusedContainerColor = FieldBg, unfocusedContainerColor = FieldBg
                            ), singleLine = true
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    KlancoreTextField(value = signo, onValueChange = { signo = it }, placeholder = "Seu signo", icon = Icons.Default.Stars)
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bio, onValueChange = { if (it.length <= 200) bio = it },
                        placeholder = { Text("Bio (máx. 200 caracteres)", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(12.dp)).background(FieldBg),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FieldFocused, unfocusedBorderColor = FieldBorder,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            cursorColor = AccentCyan, focusedContainerColor = FieldBg, unfocusedContainerColor = FieldBg
                        )
                    )

                    Spacer(Modifier.height(28.dp))

                    // ── interesse principal (clicável para trocar) ──
                    SectionLabel("Interesse principal", AccentCyan, AccentPurple)
                    Text(
                        "Toque no card para trocar",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
                    )

                    val interesseObj = listaInteresses.find { it.id == interesseAtual }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(listOf(AccentCyan.copy(0.15f), AccentPurple.copy(0.15f)))
                            )
                            .border(1.5.dp, Brush.linearGradient(listOf(AccentCyan, AccentPurple)), RoundedCornerShape(12.dp))
                            .clickable { mostrarSeletorInteresse = true }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(interesseObj?.emoji ?: "❓", fontSize = 28.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    interesseObj?.label ?: "Escolher interesse",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCyan
                                )
                            }
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Trocar",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // ── subcategorias (múltipla seleção) ──
                    AnimatedVisibility(
                        visible = subcategorias.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(Modifier.height(24.dp))

                            SectionLabel("Especialidades", AccentPurple, AccentCyan)

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Selecione uma ou mais",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                // contador
                                if (subcategoriasSelecionadas.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Brush.linearGradient(listOf(AccentPurple, AccentCyan)))
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            "${subcategoriasSelecionadas.size} selecionada(s)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            subcategorias.chunked(3).forEach { rowItems ->
                                Row(
                                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { sub ->
                                        val sel = subcategoriasSelecionadas.contains(sub.id)
                                        val scaleCard by animateFloatAsState(if (sel) 1.05f else 1f, label = "sub${sub.id}")

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .scale(scaleCard)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (sel)
                                                        Brush.linearGradient(listOf(AccentPurple.copy(0.2f), AccentCyan.copy(0.2f)))
                                                    else Brush.linearGradient(listOf(FieldBg, FieldBg))
                                                )
                                                .border(
                                                    width = if (sel) 1.5.dp else 1.dp,
                                                    brush = if (sel)
                                                        Brush.linearGradient(listOf(AccentPurple, AccentCyan))
                                                    else Brush.linearGradient(listOf(FieldBorder, FieldBorder)),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    if (sel) subcategoriasSelecionadas.remove(sub.id)
                                                    else subcategoriasSelecionadas.add(sub.id)
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(sub.emoji, fontSize = 20.sp)
                                                Spacer(Modifier.height(3.dp))
                                                Text(
                                                    sub.label,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (sel) AccentPurple else TextSecondary
                                                )
                                            }
                                        }
                                    }
                                    repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    SectionLabel("Outros Interesses", AccentCyan, AccentBlue)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "O que mais gostas? (opcional)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        // contador
                        if (outrosInteressesSelecionados.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Brush.linearGradient(listOf(AccentCyan, AccentBlue)))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    "${outrosInteressesSelecionados.size} selecionado(s)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Filtramos para não mostrar o interesse principal na lista de "outros"
                    val interessesDisponiveis = listaInteresses.filter { it.id != interesseAtual }

                    interessesDisponiveis.chunked(3).forEach { rowItems ->
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { inter ->
                                val sel = outrosInteressesSelecionados.contains(inter.id)
                                val scaleCard by animateFloatAsState(if (sel) 1.05f else 1f, label = "outros${inter.id}")

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .scale(scaleCard)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (sel)
                                                Brush.linearGradient(listOf(AccentCyan.copy(0.2f), AccentBlue.copy(0.2f)))
                                            else Brush.linearGradient(listOf(FieldBg, FieldBg))
                                        )
                                        .border(
                                            width = if (sel) 1.5.dp else 1.dp,
                                            brush = if (sel)
                                                Brush.linearGradient(listOf(AccentCyan, AccentBlue))
                                            else Brush.linearGradient(listOf(FieldBorder, FieldBorder)),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            if (sel) outrosInteressesSelecionados.remove(inter.id)
                                            else outrosInteressesSelecionados.add(inter.id)
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(inter.emoji, fontSize = 20.sp)
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            inter.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sel) AccentCyan else TextSecondary
                                        )
                                    }
                                }
                            }
                            repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                    // ================= FIM: OUTROS INTERESSES =================

                    if (erro.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(erro, color = Color.Red, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }

            // ── botão fixo ──
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(if (botaoAtivo) 10.dp else 0.dp, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            if (botaoAtivo) gradientButton
                            else Brush.linearGradient(listOf(Color.Gray.copy(0.4f), Color.Gray.copy(0.4f)))
                        )
                        .clickable(enabled = botaoAtivo) {
                            if (currentUser == null) { erro = "Usuário não autenticado"; return@clickable }
                            val idadeNum = idade.toIntOrNull()
                            if (idadeNum == null || idadeNum <= 0 || idadeNum > 120) { erro = "Idade inválida"; return@clickable }

                            val dados = hashMapOf(
                                "nome"           to nome,
                                "email"          to email,
                                "bio"            to bio,
                                "idade"          to idadeNum,
                                "sexualidade"    to sexualidade,
                                "signo"          to signo,
                                "interesse"      to interesseAtual,
                                "subcategorias"  to subcategoriasSelecionadas.toList(),
                                "outrosInteresses" to outrosInteressesSelecionados.toList()
                            )

                            firestore.collection("usuarios").document(currentUser.uid)
                                .set(dados, SetOptions.merge())
                                .addOnSuccessListener {
                                    if (modoEdicao) navController.popBackStack()
                                    else navController.navigate("lobbyPrincipal") {
                                        popUpTo("loginPrimeiraVez") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener { erro = "Erro ao salvar. Tente novamente." }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (modoEdicao) "Salvar Alterações" else "Finalizar Cadastro",
                        color = Color.White, fontWeight = FontWeight.Bold,
                        fontSize = 16.sp, letterSpacing = 1.sp
                    )
                }
            }

            RodapeUsuario(navController = navController, selected = "")
        }
    }
}

@Composable
private fun SectionLabel(text: String, color1: Color, color2: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp).height(18.dp)
                .background(Brush.verticalGradient(listOf(color1, color2)), RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 15.sp)
    }
}