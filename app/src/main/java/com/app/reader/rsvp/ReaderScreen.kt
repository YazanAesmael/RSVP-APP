package com.app.reader.rsvp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.reader.rsvp.components.ReaderScreenContent

val BgDark = Color(0xFF0F0F0F)
val SurfaceDark = Color(0xFF1C1C1E)
val TextPrimary = Color(0xFFF2F2F7)
val TextFaded = Color(0xFF8E8E93)
val AccentBlue = Color(0xFF0A84FF)

@Composable
fun RSVPScreen(
    viewModel: ReaderViewModel = viewModel(),
    onBack: () -> Unit
) {
    val currentWord by viewModel.currentWord.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val targetWpm by viewModel.targetWpm.collectAsState()
    val allWords by viewModel.allWords.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val showControls by viewModel.showControls.collectAsState()
    val isLocked by viewModel.isFocusModeLocked.collectAsState()

    ReaderScreenContent(
        currentWord = currentWord,
        isPlaying = isPlaying,
        targetWpm = targetWpm,
        allWords = allWords,
        currentIndex = currentIndex,
        timeLeft = timeLeft,
        showControls = showControls,
        isLocked = isLocked,
        onToggleLock = viewModel::toggleFocusModeLock,
        onUserInteraction = viewModel::onUserInteraction,
        onBack = onBack,
        onTogglePlay = viewModel::togglePlayPause,
        onRestart = { viewModel.loadFromRepository() },
        onSpeedChange = viewModel::updateTargetWpm,
        onSeek = viewModel::seekTo
    )
}