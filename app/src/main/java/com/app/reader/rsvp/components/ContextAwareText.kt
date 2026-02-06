package com.app.reader.rsvp.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ContextAwareText(
    words: List<String>,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    val pageSize = 24
    val pageIndex = currentIndex / pageSize

    val currentBlockState by remember(pageIndex, words) {
        derivedStateOf {
            val startIndex = (pageIndex * pageSize).coerceAtLeast(0)
            val endIndex = (startIndex + pageSize).coerceAtMost(words.size)
            val slice = words.subList(startIndex, endIndex)
            Triple(slice, startIndex, endIndex)
        }
    }

    val (visibleWords, blockStartIndex, _) = currentBlockState

    Crossfade(
        targetState = visibleWords,
        animationSpec = tween(300),
        label = "ContextTransition"
    ) { staticWords ->

        val styledText = buildAnnotatedString {
            staticWords.forEachIndexed { index, word ->
                val absoluteIndex = blockStartIndex + index
                val isTarget = absoluteIndex == currentIndex

                // --- UPDATED COLORS FOR FOCUS ---
                val color = when {
                    isTarget -> Color.White.copy(0.5f) // The anchor point
                    // Past words: Very dim, almost invisible
                    absoluteIndex < currentIndex -> Color.Gray.copy(alpha = 0.15f)
                    // Future words: Darker grey, low contrast
                    else -> Color.DarkGray.copy(alpha = 0.4f)
                }

                withStyle(
                    style = SpanStyle(
                        color = color,
                        fontWeight = if (isTarget) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp
                    )
                ) {
                    append("$word ")
                }
            }
        }

        Text(
            text = styledText,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            maxLines = 3
        )
    }
}