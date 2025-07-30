package com.example.pixelpholio

import android.R.attr.name
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


val pixelFontFamily = FontFamily(
    Font(R.font.pixel_font, FontWeight.Normal)
)

// Data class and list remain the same
data class SkillBadge(
    val name: String,
    val iconResId: Int,
    val description: String,
    val longDescription: String = "",
    val exampleUse: String = ""
)

val skillList = listOf(
    SkillBadge(
        name = "Kotlin",
        iconResId = R.drawable.ic_kotlin,
        description = "Modern, expressive language for the JVM.",
        longDescription = "Used extensively in Android development for its concise syntax, null safety, and full interoperability with Java.",
        exampleUse = "val greeting = \"Hello, Kotlin!\""
    ),
    SkillBadge(
        name = "Jetpack Compose",
        iconResId = R.drawable.ic_compose,
        description = "Modern UI toolkit for Android.",
        longDescription = "Simplifies UI development by enabling declarative programming, reactive updates, and cleaner architecture.",
        exampleUse = "@Composable fun Greeting(name: String) { Text(\"Hello $name\") }"
    ),
    SkillBadge(
        name = "Firebase",
        iconResId = R.drawable.ic_firebase,
        description = "Comprehensive Backend-as-a-Service (BaaS).",
        longDescription = "Integrated for authentication, real-time database, Firestore, cloud messaging, and analytics in Android apps.",
        exampleUse = "FirebaseAuth.getInstance().signInWithEmailAndPassword(...)"
    ),
    SkillBadge(
        name = "2D Game Development",
        iconResId = R.drawable.ic_gamedev,
        description = "Game mechanics and engine logic.",
        longDescription = "Built a 2D platformer with gravity simulation, tile-based collision detection, joystick controls, enemy behavior, and animated sprites.",
        exampleUse = "player.y += gravity * deltaTime"
    )
)


/**
 * The main screen, now with a thematic pixel-art style background and layout.
 */
@Composable
fun SkillShowcaseScreen() {
    var selectedSkill: SkillBadge? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Thematic Background Image
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Pixel art background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 2. Thematic Title
            Text(
                text = "SKILLS",
                color = Color.White,
                fontSize = 48.sp,
                fontFamily = pixelFontFamily,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            // Inside your SkillShowcaseScreen composable...
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(skillList) { skill ->
                    PixelatedSkillButton(skill = skill) {

                        selectedSkill = skill
                    }
                }
            }

            // 4. Thematic "Back" Button
            PixelatedSkillButton(
                skill = SkillBadge("BACK", R.drawable.ic_back_arrow, ""),
                onClick = {  }
            )
        }
    }

    // Display the SkillDetailDialog if a skill is selected
    selectedSkill?.let { skill ->
        SkillDetailDialog(skill = skill, onDismiss = { selectedSkill = null })
    }
}

/**
 * A new composable for the stylized, pixelated skill buttons.
 */
@Composable
fun PixelatedSkillButton(skill: SkillBadge, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RectangleShape,
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(60.dp)
            .border(2.dp, Color.White, RectangleShape),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(id = skill.iconResId),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = skill.name.uppercase(), // Uppercase for retro style
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = pixelFontFamily
            )
        }
    }
}


/**
 * The dialog, now re-styled to match the game's theme.
 */
@Composable
fun SkillDetailDialog(skill: SkillBadge, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", fontFamily = pixelFontFamily, color = Color(0xFFBBBBBB))
            }
        },
        title = {
            Text(
                text = skill.name,
                fontFamily = pixelFontFamily,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = skill.iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(54.dp).padding(bottom = 16.dp)
                )
                Text(
                    text = skill.longDescription,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                if (skill.exampleUse.isNotBlank()) {
                    Text(
                        text = "\nExample Use:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = skill.exampleUse,
                        fontSize = 14.sp
                    )
                }
            }
        },
        shape = RectangleShape,
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        modifier = Modifier.border(2.dp, Color.White, RectangleShape)
    )
}