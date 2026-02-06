package com.app.reader.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.reader.data.DocumentParser
import com.app.reader.data.ReaderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    // Two-way binding for the text box
    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    // Loading state for big files
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun updateInputText(text: String) {
        _inputText.value = text
        // Update the global repository immediately so Player can see it
        ReaderRepository.updateText(text)
    }

    fun handleFileSelection(context: Context, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            _isLoading.value = true
            
            // 1. Parse in background
            val extractedText = DocumentParser.parseFile(context, uri)
            
            // 2. Update State
            if (extractedText.isNotBlank()) {
                updateInputText(extractedText)
            }
            
            _isLoading.value = false
        }
    }
    
    fun clearText() {
        updateInputText("")
    }
}