package com.tazy.meuapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.tazy.meuapp.model.Post
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PostItem(
    post: Post,
    onLikeClick: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    var liked by remember { mutableStateOf(post.likedByUser) }
    var likesCount by remember { mutableStateOf(post.likesCount) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    val AccentCyan = Color(0xFF4DD9E8)
    val AccentPurple = Color(0xFF8B5CF6)
    val TextPrimary = Color(0xFFE8F4FF)
    val TextSecondary = Color(0xFF8BA8C0)
    val FieldBg = Color(0xFF0D1A2A).copy(alpha = 0.75f)

    val timeFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    val displayTime = post.timestamp?.toDate()?.let { timeFormat.format(it) } ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(FieldBg)
            .border(1.dp, Brush.linearGradient(listOf(AccentCyan.copy(0.5f), AccentPurple.copy(0.5f))), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header do Post
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(AccentCyan.copy(0.2f), Color.Transparent)))
                            .border(1.dp, AccentCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        if (displayTime.isNotEmpty()) {
                            Text(displayTime, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }

                if (post.authorId == currentUserId) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Excluir", tint = Color(0xFFFF5252).copy(alpha = 0.8f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Texto do Post
            Text(
                text = post.text,
                fontSize = 15.sp,
                color = TextPrimary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = TextSecondary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Footer (Likes)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        liked = !liked
                        likesCount = if (liked) likesCount + 1 else likesCount - 1
                        onLikeClick(liked)
                    }
                ) {
                    Icon(
                        imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (liked) "Descurtir" else "Curtir",
                        tint = if (liked) Color(0xFFFF5252) else TextSecondary
                    )
                }
                Text("$likesCount curtidas", fontSize = 14.sp, color = if (liked) Color(0xFFFF5252) else TextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    }
}