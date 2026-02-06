package com.app.reader.rsvp.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.reader.domain.RSVPWord
import com.app.reader.rsvp.AccentBlue
import com.app.reader.rsvp.BgDark
import com.app.reader.rsvp.TextFaded

@Composable
fun ReaderScreenContent(
    currentWord: RSVPWord,
    isPlaying: Boolean,
    targetWpm: Int,
    allWords: List<String>,
    currentIndex: Int,
    timeLeft: String,
    showControls: Boolean,
    isLocked: Boolean,
    onToggleLock: () -> Unit,
    onUserInteraction: () -> Unit,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onRestart: () -> Unit,
    onSpeedChange: (Int) -> Unit,
    onSeek: (Float) -> Unit
) {
    val uiAlpha by animateFloatAsState(
        targetValue = if (showControls) 1f else 0.05f,
        animationSpec = tween(durationMillis = 500),
        label = "FocusModeFade"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onUserInteraction
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ==========================
            // 1. TOP HEADER (Back, Time, LOCK)
            // ==========================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .alpha(uiAlpha)
            ) {
                // Back Button (Left)
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onUserInteraction()
                            onBack()
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextFaded,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Home", color = TextFaded, fontSize = 14.sp)
                }

                // Time Left (Center)
                Text(
                    text = timeLeft.uppercase(),
                    color = TextFaded,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Center)
                )

                // LOCK BUTTON (Right)
                IconButton(
                    onClick = {
                        onUserInteraction()
                        onToggleLock()
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                        contentDescription = "Toggle Focus Lock",
                        // Blue if locked, Faded if unlocked
                        tint = if (isLocked) AccentBlue else TextFaded.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ==========================
            // 2. MAIN READING STAGE
            // ==========================

            // A. Context Window (Dimmed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .alpha(uiAlpha),
                contentAlignment = Alignment.BottomCenter
            ) {
                ContextAwareText(
                    words = allWords,
                    currentIndex = currentIndex,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // B. The Main Word (Always Visible)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.width(2.dp).height(12.dp).background(Color(0xFF333333)))
                AutoSizingRSVPWord(
                    rsvpWord = currentWord,
                    maxFontSize = 32.sp
                )
                Box(modifier = Modifier.width(2.dp).height(12.dp).background(Color(0xFF333333)))
            }

            Spacer(modifier = Modifier.weight(1f))

            // ==========================
            // 3. BOTTOM CONTROL CARD
            // ==========================
            Box(modifier = Modifier.alpha(uiAlpha)) {
                PlaybackControls(
                    isPlaying = isPlaying,
                    currentIndex = currentIndex,
                    totalWords = allWords.size,
                    targetWpm = targetWpm,
                    onPlayPause = {
                        onUserInteraction()
                        onTogglePlay()
                    },
                    onRestart = {
                        onUserInteraction()
                        onRestart()
                    },
                    onSpeedChange = {
                        onUserInteraction()
                        onSpeedChange(it)
                    },
                    onSeek = {
                        onUserInteraction()
                        onSeek(it)
                    }
                )
            }
        }
    }
}