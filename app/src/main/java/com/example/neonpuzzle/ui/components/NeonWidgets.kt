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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex // Keep import just in case, but removing usage below
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

// --- OPTIMIZED CONFETTI SYSTEM ---
data class ConfettiParticle(
    var x: Float, var y: Float, var vx: Float, var vy: Float,
    var color: Color, var rotation: Float, var rotSpeed: Float, var size: Float
)

class OptimizedConfettiState(val screenHeight: Float) {
    val particles = Array(400) { 
        ConfettiParticle(0f, 0f, 0f, 0f, Color.White, 0f, 0f, 0f)
    }
    init { reset() }

    fun reset() {
        particles.forEach { p -> resetParticle(p) }
    }

    private fun resetParticle(p: ConfettiParticle) {
        p.x = Random.nextFloat() * 1400f
        p.y = -Random.nextFloat() * screenHeight * 1.5f
        p.vx = (Random.nextFloat() - 0.5f) * 4f
        p.vy = Random.nextFloat() * 8f + 5f
        p.color = listOf(PrimaryAction, SecondaryAction, AccentYellow, SuccessGreen, Color(0xFFE91E63)).random()
        p.rotation = Random.nextFloat() * 360f
        p.rotSpeed = (Random.nextFloat() - 0.5f) * 10f
        p.size = Random.nextFloat() * 25f + 15f
    }

    fun update() {
        particles.forEach { p ->
            p.x += p.vx
            p.y += p.vy
            p.rotation += p.rotSpeed
            p.vy += 0.05f 
            
            if (p.y > screenHeight + 200f) {
                p.y = -50f
                p.x = Random.nextFloat() * 1400f
                p.vy = Random.nextFloat() * 8f + 5f
            }
        }
    }
}

@Composable
fun CelebrationOverlay(visible: Boolean) {
    if (!visible) return
    
    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp.value * config.densityDpi / 160f
    
    val confettiState = remember { OptimizedConfettiState(screenHeight + 1000f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { _ -> confettiState.update() }
        }
    }

    // FIX: Removed .zIndex(Float.MAX_VALUE) so parents can control layering
    Canvas(modifier = Modifier.fillMaxSize()) {
        confettiState.particles.forEach { p ->
            rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(p.x, p.y),
                    size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.6f)
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