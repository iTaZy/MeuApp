package com.tazy.meuapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tazy.meuapp.model.Post

@Composable
fun PostItem(
    post: Post,
    onLikeClick: (Boolean) -> Unit, // passa o novo estado liked (curtido ou não)
    onDeleteClick: () -> Unit       // callback para exclusão
) {
    var liked by remember { mutableStateOf(post.likedByUser) }
    var likesCount by remember { mutableStateOf(post.likesCount) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(post.authorName, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDeleteClick) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Excluir post")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(post.text)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$likesCount curtidas")
                Spacer(modifier = Modifier.width(16.dp))
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
                        tint = if (liked) Color.Red else Color.Gray
                    )
                }
            }
        }
    }
}
