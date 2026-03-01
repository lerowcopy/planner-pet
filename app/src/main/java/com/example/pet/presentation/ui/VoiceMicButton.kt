package com.example.pet.presentation.ui

import android.util.Log
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale

@Composable
fun VoiceMicButton(
    quickTaskTitle: String,
    isRecording: Boolean,
    isModelLoading: Boolean,
    onSendText: () -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    IconButton(
        enabled = !isModelLoading,
        onClick = {
            when {
                quickTaskTitle.isNotBlank() -> {
                    Log.i("ai", "task is empty")
                    onSendText()
                }
                isRecording -> {
                    Log.i("ai", "stop voice")
                    onStopVoice()
                }
                else -> {
                    Log.i("ai", "start voice")
                    onStartVoice()
                }
            }
        }
    ) {
        Icon(
            imageVector = when {
                quickTaskTitle.isNotBlank() -> Icons.AutoMirrored.Filled.Send
                isRecording -> Icons.Default.Stop
                else -> Icons.Rounded.KeyboardVoice
            },
            contentDescription = null,
            tint = when {
                quickTaskTitle.isNotBlank() -> MaterialTheme.colorScheme.primary
                isRecording -> MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha)
                isModelLoading -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
            modifier = if (isRecording) Modifier.scale(pulseScale) else Modifier
        )
    }
}