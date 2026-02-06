package com.app.reader.data

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

object DocumentParser {

    // Must be called once in MainActivity or Application (we'll do it lazily)
    private var isInitialized = false

    suspend fun parseFile(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            PDFBoxResourceLoader.init(context)
            isInitialized = true
        }

        val mimeType = context.contentResolver.getType(uri) ?: ""
        val fileName = getFileName(uri).lowercase()

        return@withContext when {
            mimeType.contains("pdf") || fileName.endsWith(".pdf") -> {
                parsePdf(context, uri)
            }
            mimeType.contains("wordprocessingml") || fileName.endsWith(".docx") -> {
                parseDocx(context, uri)
            }
            else -> {
                readPlainText(context, uri)
            }
        }
    }

    private fun parsePdf(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Load the PDF document
                val document = PDDocument.load(inputStream)
                // Extract text
                val stripper = PDFTextStripper()
                val text = stripper.getText(document)
                document.close()
                text
            } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            "Error reading PDF: ${e.localizedMessage}"
        }
    }

    private fun parseDocx(context: Context, uri: Uri): String {
        val sb = StringBuilder()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val zipStream = ZipInputStream(inputStream)
                var entry = zipStream.nextEntry
                
                while (entry != null) {
                    // The main text in a .docx is always in "word/document.xml"
                    if (entry.name == "word/document.xml") {
                        val reader = BufferedReader(InputStreamReader(zipStream))
                        val content = reader.readText()
                        
                        // Simple Regex to extract text inside <w:t> tags
                        // This is much faster than full XML parsing for simple text
                        val pattern = "<w:t[^>]*>(.*?)</w:t>".toRegex()
                        val matches = pattern.findAll(content)
                        matches.forEach { match ->
                            sb.append(match.groupValues[1]).append(" ")
                        }
                        break // We found the text, stop unzipping
                    }
                    zipStream.closeEntry()
                    entry = zipStream.nextEntry
                }
            }
        } catch (_: Exception) {
            return "Error reading DOCX. Ensure it is not password protected."
        }
        return sb.toString()
    }

    private fun readPlainText(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { 
                it.bufferedReader().readText() 
            } ?: ""
        } catch (_: Exception) {
            "Error reading file."
        }
    }

    private fun getFileName(uri: Uri): String {
        // Basic check for extension in URI path
        return uri.path ?: ""
    }
}