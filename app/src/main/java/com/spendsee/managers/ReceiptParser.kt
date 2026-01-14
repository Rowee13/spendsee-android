package com.spendsee.managers

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class ReceiptData(
    val amount: Double?,
    val date: Long?,
    val merchantName: String?,
    val items: List<String>,
    val rawText: String
)

class ReceiptParser {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun parseReceipt(bitmap: Bitmap): ReceiptData {
        return suspendCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val data = extractReceiptData(visionText.text)
                    continuation.resume(data)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    private fun extractReceiptData(text: String): ReceiptData {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        return ReceiptData(
            amount = extractAmount(lines),
            date = extractDate(lines),
            merchantName = extractMerchant(lines),
            items = extractItems(lines),
            rawText = text
        )
    }

    private fun extractAmount(lines: List<String>): Double? {
        // Priority 1: Lines containing "TOTAL" or "AMOUNT"
        val totalKeywords = listOf("total", "amount", "balance", "grand total", "subtotal")

        for (line in lines) {
            val lowerLine = line.lowercase()
            if (totalKeywords.any { lowerLine.contains(it) }) {
                val amount = extractNumberFromLine(line)
                if (amount != null && amount > 0) {
                    return amount
                }
            }
        }

        // Priority 2: Look for currency symbols with numbers
        val currencyPattern = Regex("""[$₱€£¥₩₹]\s*([\d,]+\.?\d*)""")
        val amounts = mutableListOf<Double>()

        for (line in lines) {
            val matches = currencyPattern.findAll(line)
            for (match in matches) {
                val numberStr = match.groupValues[1].replace(",", "")
                numberStr.toDoubleOrNull()?.let { amounts.add(it) }
            }
        }

        // Return the largest amount found (likely the total)
        return amounts.maxOrNull()
    }

    private fun extractNumberFromLine(line: String): Double? {
        // Extract number with optional currency symbol and decimal
        val numberPattern = Regex("""([\d,]+\.?\d*)""")
        val matches = numberPattern.findAll(line)

        for (match in matches) {
            val numberStr = match.value.replace(",", "")
            val number = numberStr.toDoubleOrNull()
            if (number != null && number > 0) {
                return number
            }
        }

        return null
    }

    private fun extractDate(lines: List<String>): Long? {
        // Common date patterns
        val datePatterns = listOf(
            SimpleDateFormat("MM/dd/yyyy", Locale.US),
            SimpleDateFormat("dd/MM/yyyy", Locale.US),
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("MM-dd-yyyy", Locale.US),
            SimpleDateFormat("dd-MM-yyyy", Locale.US),
            SimpleDateFormat("MMM dd, yyyy", Locale.US),
            SimpleDateFormat("dd MMM yyyy", Locale.US),
            SimpleDateFormat("MMMM dd, yyyy", Locale.US),
            SimpleDateFormat("MM/dd/yy", Locale.US),
            SimpleDateFormat("dd/MM/yy", Locale.US)
        )

        for (line in lines.take(10)) { // Check first 10 lines
            for (pattern in datePatterns) {
                try {
                    pattern.isLenient = false
                    val date = pattern.parse(line)
                    if (date != null) {
                        return date.time
                    }
                } catch (e: Exception) {
                    // Try next pattern or line
                    continue
                }
            }
        }

        // If no date found, return current date
        return System.currentTimeMillis()
    }

    private fun extractMerchant(lines: List<String>): String? {
        // Look for merchant name in first 5 lines
        // Exclude lines with common receipt keywords
        val excludeKeywords = listOf(
            "receipt", "tax", "total", "subtotal", "amount", "date", "time",
            "cashier", "invoice", "transaction", "payment", "change", "cash"
        )

        for (line in lines.take(5)) {
            val lowerLine = line.lowercase()

            // Skip lines with excluded keywords
            if (excludeKeywords.any { lowerLine.contains(it) }) {
                continue
            }

            // Skip lines with mostly numbers or special characters
            val alphaCount = line.count { it.isLetter() }
            val totalCount = line.length

            if (totalCount > 3 && alphaCount.toFloat() / totalCount > 0.6f) {
                return line
            }
        }

        return null
    }

    private fun extractItems(lines: List<String>): List<String> {
        val items = mutableListOf<String>()

        // Look for lines that have a pattern: Item name + price
        // Pattern: text followed by a number (price)
        val itemPattern = Regex("""^(.+?)\s+([\d,]+\.?\d*)$""")

        for (line in lines) {
            val match = itemPattern.find(line)
            if (match != null) {
                val itemName = match.groupValues[1].trim()
                val price = match.groupValues[2]

                // Filter out lines that are likely not items
                val lowerName = itemName.lowercase()
                if (!lowerName.contains("total") &&
                    !lowerName.contains("tax") &&
                    !lowerName.contains("subtotal") &&
                    itemName.length > 2) {
                    items.add("$itemName - $price")

                    // Limit to 10 items
                    if (items.size >= 10) break
                }
            }
        }

        return items
    }
}
