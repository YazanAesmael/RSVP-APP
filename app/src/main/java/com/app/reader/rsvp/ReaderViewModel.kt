package com.app.reader.rsvp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.reader.data.ReaderRepository
import com.app.reader.domain.WordProcessor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

class ReaderViewModel : ViewModel() {

    // --- Data State ---
    private val _currentWord = MutableStateFlow(WordProcessor.processWord("Ready"))
    val currentWord = _currentWord.asStateFlow()

    private val _allWords = MutableStateFlow<List<String>>(emptyList())
    val allWords = _allWords.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    // --- Playback State ---
    private val _targetWpm = MutableStateFlow(300)
    val targetWpm = _targetWpm.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _timeLeft = MutableStateFlow("0s")
    val timeLeft = _timeLeft.asStateFlow()

    // --- FOCUS MODE STATE ---
    private val _showControls = MutableStateFlow(true)
    val showControls = _showControls.asStateFlow()

    // Lock State
    private val _isFocusModeLocked = MutableStateFlow(false)
    val isFocusModeLocked = _isFocusModeLocked.asStateFlow()

    private var playbackJob: Job? = null
    private var focusModeJob: Job? = null

    private val fastPairs = setOf(
        "of the", "in the", "to the", "on the", "and the", "for the",
        "to be", "is a", "with a", "at the", "from the", "by the"
    )

    init {
        loadFromRepository()
    }

    // --- ACTIONS ---

    fun onUserInteraction() {
        _showControls.value = true
        if (_isPlaying.value) {
            startFocusTimer()
        }
    }

    fun toggleFocusModeLock() {
        _isFocusModeLocked.value = !_isFocusModeLocked.value
        onUserInteraction() // Reset timer logic based on new lock state
    }

    private fun startFocusTimer() {
        focusModeJob?.cancel()

        // If locked, we NEVER start the timer to hide controls
        if (_isFocusModeLocked.value) return

        focusModeJob = viewModelScope.launch {
            delay(3000) // Wait 3 seconds
            // Double check lock state inside coroutine just in case
            if (_isPlaying.value && !_isFocusModeLocked.value) {
                _showControls.value = false
            }
        }
    }

    fun loadFromRepository() {
        val text = ReaderRepository.textContent.value
        loadText(text.ifBlank { "No text found. Please go back and add some text." })
    }

    fun loadText(text: String) {
        pause()
        val list = text.replace("\n", " ").split(" ").filter { it.isNotBlank() }
        _allWords.value = list
        _currentIndex.value = 0
        if (list.isNotEmpty()) {
            _currentWord.value = WordProcessor.processWord(list[0])
        }
        updateTimeLeft()
    }

    fun togglePlayPause() {
        onUserInteraction()
        if (_isPlaying.value) {
            pause()
            val rewind = 5
            val newIndex = (_currentIndex.value - rewind).coerceAtLeast(0)
            _currentIndex.value = newIndex
            if (_allWords.value.isNotEmpty()) {
                _currentWord.value = WordProcessor.processWord(_allWords.value[newIndex])
            }
        } else {
            play()
        }
    }

    fun updateTargetWpm(wpm: Int) {
        onUserInteraction()
        _targetWpm.value = wpm.coerceIn(100, 1000)
        updateTimeLeft()
    }

    fun seekTo(progress: Float) {
        onUserInteraction()
        val list = _allWords.value
        if (list.isEmpty()) return
        val newIndex = (progress * list.size).toInt().coerceIn(0, list.lastIndex)
        _currentIndex.value = newIndex
        _currentWord.value = WordProcessor.processWord(list[newIndex])
        updateTimeLeft()
    }

    private fun play() {
        if (_isPlaying.value) return
        _isPlaying.value = true
        startFocusTimer()

        var rampFactor = 0.5f
        playbackJob = viewModelScope.launch {
            val list = _allWords.value
            while (_isPlaying.value && _currentIndex.value < list.size) {
                val index = _currentIndex.value
                val currentString = list[index]
                val nextString = if (index + 1 < list.size) list[index + 1] else ""

                _currentWord.value = WordProcessor.processWord(currentString)

                val wpm = _targetWpm.value
                if (rampFactor < 1.0f) rampFactor += 0.1f
                val currentSpeed = (wpm * min(rampFactor, 1.0f)).toInt()

                val delayMs = calculateHumanizedDelay(currentString, nextString, currentSpeed)
                delay(delayMs)

                if (_isPlaying.value) {
                    _currentIndex.value = _currentIndex.value + 1

                    // --- FIX: Update Time Left on EVERY word ---
                    // Removed the "% 10" check to fix the jerky timer
                    updateTimeLeft()
                }
            }
            if (_currentIndex.value >= list.size) {
                pause()
                _currentIndex.value = 0

                if (list.isNotEmpty()) {
                    _currentWord.value = WordProcessor.processWord(list[0])
                }
            }
        }
    }

    private fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null

        focusModeJob?.cancel()
        _showControls.value = true
    }

    private fun updateTimeLeft() {
        val remainingWords = _allWords.value.size - _currentIndex.value
        if (remainingWords <= 0 || _targetWpm.value == 0) {
            _timeLeft.value = "Done"
            return
        }
        val minutes = remainingWords.toFloat() / _targetWpm.value
        val totalSeconds = (minutes * 60).toInt()
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        _timeLeft.value = "${m}m ${s}s left"
    }

    private fun calculateHumanizedDelay(word: String, nextWord: String, wpm: Int): Long {
        if (wpm == 0) return 100L
        val baseDelay = (60_000 / wpm).toLong()
        var duration = baseDelay.toFloat()

        val cleanWord = word.filter { it.isLetterOrDigit() }.lowercase()
        val cleanNext = nextWord.filter { it.isLetterOrDigit() }.lowercase()
        val length = cleanWord.length
        val lastChar = word.lastOrNull()

        duration *= when {
            length <= 2 -> 1f
            length <= 4 -> 1.1f
            length in 5..8 -> 1.3f
            length in 9..12 -> 1.5f
            else -> 1.7f
        }

        val pair = "$cleanWord $cleanNext"
        if (fastPairs.contains(pair)) duration *= 1.1f
        if (length < 4 && cleanNext.length > 10) duration *= 1.3f

        when (lastChar) {
            '.', '!', '?' -> duration *= 2.3f
            ',', ';', ':' -> duration *= 1.5f
            '-', '—' -> duration *= 1.4f
            '"', '”' -> duration *= 1.2f
        }

        return duration.toLong()
    }
}