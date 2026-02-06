package com.app.reader.rsvp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.app.reader.domain.RSVPWord

@Composable
fun AutoSizingRSVPWord(
    modifier: Modifier = Modifier,
    rsvpWord: RSVPWord,
    maxFontSize: TextUnit = 42.sp
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val availableWidthPx = with(density) { maxWidth.toPx() }
        val maxHalfWidthPx = (availableWidthPx / 2f) * 0.85f

        val baseStyle = LocalTextStyle.current.copy(
            fontSize = maxFontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )

        val leftRes = textMeasurer.measure(rsvpWord.leftPart, baseStyle)
        val rightRes = textMeasurer.measure(rsvpWord.rightPart, baseStyle)
        val pivotRes = textMeasurer.measure(rsvpWord.pivotChar, baseStyle)
        val pivotHalfWidth = pivotRes.size.width / 2f

        // Check which side is longer (Left or Right) including half the pivot
        val maxContentHalfWidth = maxOf(
            leftRes.size.width + pivotHalfWidth,
            rightRes.size.width + pivotHalfWidth
        )

        // Only scale DOWN. Never scale UP.
        val scaleFactor = if (maxContentHalfWidth > maxHalfWidthPx) {
            maxHalfWidthPx / maxContentHalfWidth
        } else {
            1f
        }

        val finalFontSize = maxFontSize * scaleFactor

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = rsvpWord.leftPart,
                color = Color.White,
                fontSize = finalFontSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = rsvpWord.pivotChar,
                color = Color.Red,
                fontSize = finalFontSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.wrapContentWidth()
            )

            Text(
                text = rsvpWord.rightPart,
                color = Color.White,
                fontSize = finalFontSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}