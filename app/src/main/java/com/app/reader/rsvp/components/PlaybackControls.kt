package com.app.reader.rsvp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.reader.rsvp.BgDark
import com.app.reader.rsvp.SurfaceDark
import com.app.reader.rsvp.TextFaded
import com.app.reader.rsvp.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    currentIndex: Int,
    totalWords: Int,
    targetWpm: Int,
    onPlayPause: () -> Unit,
    onRestart: () -> Unit,
    onSpeedChange: (Int) -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local state for smooth dragging
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Sync scrubber only when not dragging
    val currentProgress = if (totalWords > 0) currentIndex.toFloat() / totalWords else 0f
    if (!isDragging) {
        sliderPosition = currentProgress
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                SurfaceDark,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(24.dp)
    ) {
        // --- A. PROGRESS SCRUBBER ---
        Slider(
            value = sliderPosition,
            onValueChange = {
                isDragging = true
                sliderPosition = it
                onSeek(it)
            },
            onValueChangeFinished = {
                isDragging = false
                onSeek(sliderPosition)
            },
            modifier = Modifier.fillMaxWidth().height(20.dp),
            colors = SliderDefaults.colors(
                thumbColor = TextPrimary,
                activeTrackColor = TextPrimary,
                inactiveTrackColor = Color.Black.copy(alpha = 0.3f)
            ),
            thumb = {
                Box(modifier = Modifier.size(16.dp).background(TextPrimary, CircleShape))
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    colors = SliderDefaults.colors(
                        activeTrackColor = TextPrimary,
                        inactiveTrackColor = Color.Black.copy(alpha = 0.3f)
                    ),
                    enabled = true,
                    sliderState = sliderState,
                    modifier = Modifier.height(4.dp).clip(RoundedCornerShape(2.dp))
                )
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${currentIndex + 1}", color = TextFaded, fontSize = 12.sp)
            Text("$totalWords", color = TextFaded, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- B. MAIN PLAYBACK BUTTONS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onRestart) {
                Icon(Icons.Default.Refresh, "Restart", tint = TextFaded, modifier = Modifier.size(24.dp))
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(TextPrimary)
                    .clickable { onPlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = BgDark,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(onClick = { /* Settings */ }) {
                Icon(Icons.Rounded.Settings, "Settings", tint = TextFaded, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- C. SPEED CONTROL (Minimal Capsule) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. WPM Value (Left Side)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$targetWpm",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "wpm",
                    color = TextFaded,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Slider (Fills Rest, Monochrome)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Slider(
                    value = targetWpm.toFloat(),
                    onValueChange = { onSpeedChange(it.toInt()) },
                    valueRange = 100f..1000f,
                    steps = 0, // No dots
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White, // White track
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                    ),
                    thumb = {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(20.dp).shadow(4.dp, CircleShape),
                            border = null
                        ) {}
                    },
                    track = { sliderState ->
                        SliderDefaults.Track(
                            colors = SliderDefaults.colors(
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            ),
                            enabled = true,
                            sliderState = sliderState,
                            modifier = Modifier
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                )
            }
        }
    }
}