package com.app.reader.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

val TestText = """
    The premise of this book is that doing well with money has a little to do with how smart you are and a lot to do with how you behave. And behavior is hard to teach, even to really smart people. A genius who loses control of their emotions can be a financial disaster. The opposite is also true. Ordinary folks with no financial education can be wealthy if they have a handful of behavioral skills that have nothing to do with formal measures of intelligence.

    Financial success is not a hard science. It’s a soft skill, where how you behave is more important than what you know. I call this soft skill the psychology of money. The aim of this book is to use short chapters to convince you that soft skills are more important than the technical side of money. We treat money like physics (with rules and laws) when we should treat it like psychology (with emotions and nuance).
    
    To grasp why people bury themselves in debt you don’t need to study interest rates; you need to study the history of greed, insecurity, and optimism. To get why investors sell at the bottom of a bear market you don’t need to study the math of expected future returns; you need to think about the agony of looking at your family and wondering if your investments are imperiling their future.
    
    We all do crazy things with money, because we’re all relatively new to this game and what looks crazy to you might make sense to me. But no one is crazy—we all make decisions based on our own unique experiences that seem to make sense to us in a given moment.
""".trimIndent()

// A simple Singleton to hold the data "in memory" while the app runs.
object ReaderRepository {
    private val _textContent = MutableStateFlow("")
    val textContent = _textContent.asStateFlow()

    fun updateText(newText: String) {
        _textContent.value = newText
    }

    fun clear() {
        _textContent.value = ""
    }
}