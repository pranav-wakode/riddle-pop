package com.example.neonpuzzle.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.neonpuzzle.ui.theme.*
import kotlin.random.Random

// --- 3D "Gummy" Button ---
@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = PrimaryAction
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offset by animateDpAsState(if (isPressed) 4.dp else 0.dp, label = "press")

    Box(
        modifier = modifier
            .padding(8.dp)
            .height(60.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.7f))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = offset)
                .clip(RoundedCornerShape(16.dp))
                .background(color)
                .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                style = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            )
        }
    }
}

// --- Friendly Card ---
@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

// --- OPTIMIZED CONFETTI SYSTEM (No Lag) ---
class ConfettiState {
    var particles = generateParticles(200) // 200 is plenty if they move smooth
    var lastFrameTime = 0L

    fun update(currentTime: Long) {
        val dt = if (lastFrameTime == 0L) 0f else (currentTime - lastFrameTime) / 1_000_000_000f
        lastFrameTime = currentTime
        
        particles.forEach { p ->
            p.y += p.vy * 5f // Speed factor
            p.rotation += p.rotSpeed
            
            // Loop logic
            if (p.y > 2500f) {
                p.y = -50f
                p.x = Random.nextFloat() * 1000f
                p.vy = Random.nextFloat() * 5f + 5f
            }
        }
    }

    private fun generateParticles(count: Int) = Array(count) {
        ConfettiParticle(
            x = Random.nextFloat() * 1080f,
            y = Random.nextFloat() * -1000f,
            vx = 0f,
            vy = Random.nextFloat() * 5f + 5f,
            color = listOf(PrimaryAction, SecondaryAction, AccentYellow, SuccessGreen).random(),
            rotation = Random.nextFloat() * 360f,
            rotSpeed = (Random.nextFloat() - 0.5f) * 10f,
            size = Random.nextFloat() * 20f + 15f
        )
    }
}

data class ConfettiParticle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    val color: Color,
    var rotation: Float,
    var rotSpeed: Float,
    val size: Float
)

@Composable
fun CelebrationOverlay(visible: Boolean) {
    if (!visible) return
    
    val confettiState = remember { ConfettiState() }
    
    // Animation loop using withFrameNanos for smooth 60fps
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { time ->
                confettiState.update(time)
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize().zIndex(100f)) {
        confettiState.particles.forEach { p ->
            rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(p.x, p.y),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}

@Composable
fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label, 
            color = TextSecondary, 
            fontSize = 14.sp, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(2.dp, if (isError) ErrorRed else SecondaryAction.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        )
    }
}