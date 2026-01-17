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
        val amounts = mutableMapOf<Double, Int>() // amount to priority score

        // Priority 1: Look for TOTAL with amount on same line (highest priority)
        val totalPattern = Regex("""(total|amount|balance|grand\s*total|net\s*total|sum)[:\s]*[$₱€£¥₩₹]?\s*([\d,]+\.?\d{0,2})""", RegexOption.IGNORE_CASE)

        for ((index, line) in lines.withIndex()) {
            val match = totalPattern.find(line)
            if (match != null) {
                val amountStr = match.groupValues[2].replace(",", "")
                val amount = amountStr.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    // Higher priority for "total" found earlier in receipt
                    val priority = 1000 - index
                    amounts[amount] = amounts.getOrDefault(amount, 0) + priority
                }
            }
        }

        // Priority 2: Look for amount patterns with currency symbols
        val currencyPattern = Regex("""[$₱€£¥₩₹]\s*([\d,]+\.?\d{0,2})""")

        for ((index, line) in lines.withIndex()) {
            // Skip if line already matched as total
            if (totalPattern.find(line) != null) continue

            val matches = currencyPattern.findAll(line)
            for (match in matches) {
                val amountStr = match.groupValues[1].replace(",", "")
                val amount = amountStr.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    // Lower priority than explicit totals
                    val priority = 500 - index
                    amounts[amount] = amounts.getOrDefault(amount, 0) + priority
                }
            }
        }

        // Priority 3: Look for numbers with two decimal places (common for money)
        val decimalPattern = Regex("""(\d{1,6}\.\d{2})(?!\d)""")

        for ((index, line) in lines.withIndex()) {
            val lowerLine = line.lowercase()
            // Only check if line might contain total
            if (lowerLine.contains("total") || lowerLine.contains("amount") ||
                lowerLine.contains("balance") || lowerLine.contains("pay")) {

                val matches = decimalPattern.findAll(line)
                for (match in matches) {
                    val amount = match.groupValues[1].toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        val priority = 300 - index
                        amounts[amount] = amounts.getOrDefault(amount, 0) + priority
                    }
                }
            }
        }

        // Return the amount with highest priority score
        return amounts.maxByOrNull { it.value }?.key
    }

    private fun extractDate(lines: List<String>): Long? {
        val calendar = Calendar.getInstance()

        // Date regex patterns to find within lines
        val dateRegexPatterns = listOf(
            // MM/DD/YYYY or MM-DD-YYYY
            Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{4})"""),
            // DD/MM/YYYY or DD-MM-YYYY
            Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{4})"""),
            // YYYY-MM-DD
            Regex("""(\d{4})[/-](\d{1,2})[/-](\d{1,2})"""),
            // MM/DD/YY
            Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{2})(?!\d)"""),
            // Month names: Jan 15, 2024 or January 15, 2024
            Regex("""(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*[\s,]+(\d{1,2})[\s,]+(\d{4})""", RegexOption.IGNORE_CASE),
            // 15 Jan 2024
            Regex("""(\d{1,2})[\s,]+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*[\s,]+(\d{4})""", RegexOption.IGNORE_CASE)
        )

        // Date format parsers
        val dateFormats = mapOf(
            0 to SimpleDateFormat("MM/dd/yyyy", Locale.US),
            1 to SimpleDateFormat("dd/MM/yyyy", Locale.US),
            2 to SimpleDateFormat("yyyy-MM-dd", Locale.US),
            3 to SimpleDateFormat("MM/dd/yy", Locale.US),
            4 to SimpleDateFormat("MMM dd, yyyy", Locale.US),
            5 to SimpleDateFormat("dd MMM yyyy", Locale.US)
        )

        // Try to find date in first 15 lines
        for (line in lines.take(15)) {
            for ((patternIndex, regex) in dateRegexPatterns.withIndex()) {
                val match = regex.find(line)
                if (match != null) {
                    try {
                        val dateStr = match.value
                        val format = dateFormats[patternIndex]
                        if (format != null) {
                            format.isLenient = false
                            val parsedDate = format.parse(dateStr)
                            if (parsedDate != null) {
                                calendar.time = parsedDate

                                // Try to extract time from same line
                                val timeMatch = extractTime(line)
                                if (timeMatch != null) {
                                    calendar.set(Calendar.HOUR_OF_DAY, timeMatch.first)
                                    calendar.set(Calendar.MINUTE, timeMatch.second)
                                }

                                return calendar.timeInMillis
                            }
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }
            }
        }

        // If no date found, return current date with any time found
        for (line in lines.take(15)) {
            val timeMatch = extractTime(line)
            if (timeMatch != null) {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.HOUR_OF_DAY, timeMatch.first)
                calendar.set(Calendar.MINUTE, timeMatch.second)
                return calendar.timeInMillis
            }
        }

        return System.currentTimeMillis()
    }

    private fun extractTime(line: String): Pair<Int, Int>? {
        // Time patterns: HH:MM AM/PM or HH:MM (24-hour)
        val timePatterns = listOf(
            // 12-hour format with AM/PM
            Regex("""(\d{1,2}):(\d{2})\s*(AM|PM)""", RegexOption.IGNORE_CASE),
            // 24-hour format
            Regex("""(\d{1,2}):(\d{2})(?!\d)""")
        )

        for (regex in timePatterns) {
            val match = regex.find(line)
            if (match != null) {
                try {
                    var hour = match.groupValues[1].toInt()
                    val minute = match.groupValues[2].toInt()

                    // Handle AM/PM if present
                    if (match.groupValues.size > 3) {
                        val amPm = match.groupValues[3].uppercase()
                        if (amPm == "PM" && hour < 12) {
                            hour += 12
                        } else if (amPm == "AM" && hour == 12) {
                            hour = 0
                        }
                    }

                    // Validate hour and minute
                    if (hour in 0..23 && minute in 0..59) {
                        return Pair(hour, minute)
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        }

        return null
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
