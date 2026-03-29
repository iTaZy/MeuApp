package com.tazy.meuapp

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ComponenteFotoPerfil() {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var fotoUrl by remember { mutableStateOf<String?>(null) }
    var fazendoUpload by remember { mutableStateOf(false) }

    // Cores Klancore
    val AccentCyan = Color(0xFF4DD9E8)
    val FieldBg = Color(0xFF0D1A2A)

    // 1. Busca a foto atual do usuário no Firebase ao carregar a tela
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener { doc ->
                    fotoUrl = doc.getString("fotoPerfil")
                }
        }
    }

    // 2. O "Lançador" que abre a galeria do celular
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // Se o usuário escolheu uma foto, começamos o upload!
            fazendoUpload = true

            MediaManager.get().upload(uri)
                // 👇 COLOQUE AQUI O NOME DO PRESET QUE VOCÊ CRIOU LÁ NO CLOUDINARY (ex: ml_default)
                .unsigned("COLOQUE_AQUI_SEU_PRESET")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        // Deu certo! Pegamos a URL segura da imagem
                        val urlSegura = resultData["secure_url"] as String

                        // Salvamos a URL no Firebase do usuário
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            firestore.collection("usuarios").document(uid)
                                .update("fotoPerfil", urlSegura)
                                .addOnSuccessListener {
                                    fotoUrl = urlSegura // Atualiza a imagem na tela
                                    fazendoUpload = false
                                }
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("Upload", "Erro: ${error.description}")
                        fazendoUpload = false
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                }).dispatch()
        }
    }

    // 3. O Visual do Círculo da Foto
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(FieldBg)
            .border(2.dp, AccentCyan, CircleShape)
            .clickable(enabled = !fazendoUpload) {
                // Ao clicar, abre a galeria pedindo apenas imagens
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
        contentAlignment = Alignment.Center
    ) {
        if (fazendoUpload) {
            CircularProgressIndicator(color = AccentCyan)
        } else if (fotoUrl.isNullOrEmpty()) {
            Icon(Icons.Default.Person, contentDescription = "Sem foto", tint = Color.Gray, modifier = Modifier.size(60.dp))
        } else {
            // Desenha a foto da internet usando a biblioteca Coil
            AsyncImage(
                model = fotoUrl,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop, // Corta a imagem para caber redondinha
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}