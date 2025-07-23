package com.example.pixelpholio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SkillBadge(
    val name: String,
    val iconResId: Int,
    val description: String
)

val skillList = listOf(
    SkillBadge("Jetpack Compose", R.drawable.ic_compose, "Modern UI toolkit for Android"),
    SkillBadge("Firebase", R.drawable.ic_firebase, "Realtime DB, Auth, and Firestore"),
    SkillBadge("Kotlin", R.drawable.ic_kotlin, "Primary language used"),
    SkillBadge("UI/UX", R.drawable.ic_uiux, "Clean, intuitive app design"),
    SkillBadge("Debugging", R.drawable.ic_debug, "Efficient bug fixing"),
//    SkillBadge("Canvas", R.drawable.ic_canvas, "Custom drawing & game logic"),
    SkillBadge("Version Control", R.drawable.ic_git, "Git & GitHub workflows")
)

@Composable
fun SkillShowcaseScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text(
            text = "🎮 Skills Showcase",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(skillList) { skill ->
                SkillCard(skill)
            }
        }
    }
}

@Composable
fun SkillCard(skill: SkillBadge) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), shape = MaterialTheme.shapes.medium)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = skill.iconResId),
            contentDescription = skill.name,
            modifier = Modifier
                .size(48.dp)
                .padding(bottom = 8.dp)
        )
        Text(skill.name, color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(skill.description, color = Color.LightGray, fontSize = 12.sp)
    }
}
