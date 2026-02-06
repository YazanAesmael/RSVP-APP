package com.app.reader.home

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.reader.rsvp.AccentBlue
import com.app.reader.rsvp.BgDark
import com.app.reader.rsvp.SurfaceDark
import com.app.reader.rsvp.TextFaded
import com.app.reader.rsvp.TextPrimary

@Composable
fun HomeScreen(
    onStartReading: () -> Unit,
    viewModel: HomeViewModel = viewModel() // Inject VM
) {
    val context = LocalContext.current

    // Collect VM State
    val inputText by viewModel.inputText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // File Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        // Delegate logic to ViewModel
        viewModel.handleFileSelection(context, uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FocusReader",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "What do you want to read?",
                    color = TextFaded,
                    fontSize = 16.sp
                )
            }

            // Clear Button (Visible only if text exists)
            if (inputText.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearText() }) {
                    Icon(Icons.Rounded.Delete, null, tint = TextFaded)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- INPUT AREA ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            if (inputText.isEmpty() && !isLoading) {
                Text(
                    text = "Paste text or upload a PDF/Doc...",
                    color = TextFaded.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
            }

            if (isLoading) {
                // Loading State in Center
                CircularProgressIndicator(
                    color = AccentBlue,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Text Field
                BasicTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(AccentBlue),
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Floating Paste Button
            if (!isLoading) {
                FloatingActionButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = clipboard.primaryClip
                        if (clipData != null && clipData.itemCount > 0) {
                            val text = clipData.getItemAt(0).text.toString()
                            viewModel.updateInputText(text)
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(48.dp),
                    containerColor = Color(0xFF333333),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Rounded.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ACTIONS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upload Button
            Button(
                // Filter for common text types
                onClick = {
                    launcher.launch(arrayOf(
                        "application/pdf",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                        "text/plain"
                    ))
                },
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Rounded.UploadFile, null, tint = AccentBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Doc", color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // START BUTTON
        Button(
            onClick = onStartReading,
            enabled = inputText.isNotBlank() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                disabledContainerColor = SurfaceDark
            )
        ) {
            Icon(Icons.Rounded.PlayArrow, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Start Reading",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (inputText.isNotBlank()) Color.White else TextFaded
            )
        }
    }
}