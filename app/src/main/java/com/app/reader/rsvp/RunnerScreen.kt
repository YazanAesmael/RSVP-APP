package com.app.reader.rsvp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.reader.data.ReaderRepository
import com.app.reader.domain.WordProcessor
import com.app.reader.rsvp.components.AutoSizingRSVPWord

@Composable
fun RunnerScreen(
    viewModel: ReaderViewModel = viewModel(),
    onBack: () -> Unit
) {
    val currentWord by viewModel.currentWord.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val targetWpm by viewModel.targetWpm.collectAsState()
    val allWords by viewModel.allWords.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()

    LaunchedEffect(Unit) {
        val textToRead = ReaderRepository.textContent.value
        if (textToRead.isNotBlank()) {
            viewModel.loadText(textToRead)
        } else {
            viewModel.loadText("No text loaded.")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
    ) {
        // --- 1. HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBack() }
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

            Text(
                text = timeLeft.uppercase(),
                color = TextFaded,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 2. INTEGRATED SCRUBBER & DISPLAY ---
        // This acts as both the Reader and the Scrubber
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), // Height for the big word
            contentAlignment = Alignment.Center
        ) {
            // Visual Focus Line (Behind the text)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(Color(0xFF222222), CircleShape)
            )

            IntegratedScrubber(
                allWords = allWords,
                currentIndex = currentIndex,
                onSeek = { index ->
                    val progress = index.toFloat() / allWords.size.coerceAtLeast(1)
                    viewModel.seekTo(progress)
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 3. BOTTOM CONTROLS ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(vertical = 24.dp)
        ) {
            // Playback Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.loadText(ReaderRepository.textContent.value) }) {
                    Icon(Icons.Default.Refresh, "Restart", tint = TextFaded)
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(TextPrimary)
                        .clickable { viewModel.togglePlayPause() },
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
                    Icon(Icons.Rounded.Speed, "Speed", tint = TextFaded)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Speed Slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Speed", color = TextFaded, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Slider(
                    value = targetWpm.toFloat(),
                    onValueChange = { viewModel.updateTargetWpm(it.toInt()) },
                    valueRange = 100f..1000f,
                    steps = 18,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = AccentBlue,
                        activeTrackColor = AccentBlue,
                        inactiveTrackColor = TextFaded.copy(alpha = 0.2f)
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("$targetWpm", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

/**
 * INTEGRATED SCRUBBER
 * Combines the "Main Display" and the "Scroll List" into one.
 */
@Composable
fun IntegratedScrubber(
    allWords: List<String>,
    currentIndex: Int,
    onSeek: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    // The active word takes up 85% of the screen.
    // Neighbors fill the remaining 15% (split 7.5% left, 7.5% right)
    val activeItemWidth = screenWidthDp * 0.85f

    // Padding to ensure the first item can be scrolled to the center
    val centerPadding = (screenWidthDp - activeItemWidth) / 2

    // 1. Auto-Scroll when playing
    LaunchedEffect(currentIndex) {
        if (!listState.isScrollInProgress) {
            // Animate scroll for smoother look, or scrollToItem for snap
            listState.scrollToItem(currentIndex)
        }
    }

    // 2. Seek when dragging
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .collect { index -> onSeek(index) }
        }
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = centerPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        items(allWords.size) { index ->
            val isSelected = index == currentIndex
            val word = allWords[index]

            if (isSelected) {
                // --- THE MAIN DISPLAY (Current Word) ---
                // We use a fixed width container so AutoSizingRSVPWord can align the pivot perfectly
                Box(
                    modifier = Modifier
                        .width(activeItemWidth)
                        .zIndex(1f), // Ensure it pops out
                    contentAlignment = Alignment.Center
                ) {
                    // We must process the word to get the pivot logic
                    val rsvpWord = remember(word) { WordProcessor.processWord(word) }

                    AutoSizingRSVPWord(
                        rsvpWord = rsvpWord,
                        maxFontSize = 56.sp
                    )
                }
            } else {
                // --- SIDE WORDS (Context) ---
                // Simple faded text
                Text(
                    text = word,
                    color = TextFaded.copy(alpha = 0.3f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}