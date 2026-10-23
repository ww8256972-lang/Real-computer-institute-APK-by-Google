package com.example.core

import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.experimental.and

object SecurityUtils {

    private val secureRandom = SecureRandom()
    private const val CHARS_UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    private const val CHARS_LOWER = "abcdefghijkmnopqrstuvwxyz"
    private const val CHARS_DIGITS = "23456789"
    private const val CHARS_SYMBOLS = "!@#$%^&*-_=+"

    /**
     * Computes SHA-256 hash of a string with salt.
     */
    fun hashPassword(password: String, salt: String = "EduSync_Institute_2026_Salt"): String {
        val input = "$salt:$password"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies if input password matches the stored hash.
     */
    fun verifyPassword(password: String, storedHash: String, salt: String = "EduSync_Institute_2026_Salt"): Boolean {
        return hashPassword(password, salt) == storedHash
    }

    /**
     * Generates a strong, random password.
     */
    fun generateStrongPassword(length: Int = 14): String {
        val allChars = CHARS_UPPER + CHARS_LOWER + CHARS_DIGITS + CHARS_SYMBOLS
        val sb = java.lang.StringBuilder(length)
        // Ensure at least one from each category
        sb.append(CHARS_UPPER[secureRandom.nextInt(CHARS_UPPER.length)])
        sb.append(CHARS_LOWER[secureRandom.nextInt(CHARS_LOWER.length)])
        sb.append(CHARS_DIGITS[secureRandom.nextInt(CHARS_DIGITS.length)])
        sb.append(CHARS_SYMBOLS[secureRandom.nextInt(CHARS_SYMBOLS.length)])

        for (i in 4 until length) {
            sb.append(allChars[secureRandom.nextInt(allChars.length)])
        }
        // Shuffle characters
        val list = sb.toList().shuffled(secureRandom)
        return list.joinToString("")
    }

    /**
     * Calculates password strength score (0 to 100) and feedback label.
     */
    fun evaluatePasswordStrength(password: String): Pair<Int, String> {
        if (password.isEmpty()) return 0 to "Empty"
        var score = 0
        if (password.length >= 8) score += 20
        if (password.length >= 12) score += 20
        if (password.any { it.isUpperCase() }) score += 15
        if (password.any { it.isLowerCase() }) score += 15
        if (password.any { it.isDigit() }) score += 15
        if (password.any { !it.isLetterOrDigit() }) score += 15

        val label = when {
            score >= 80 -> "Strong"
            score >= 50 -> "Medium"
            else -> "Weak"
        }
        return score to label
    }

    /**
     * Sanitizes spreadsheet cell strings to prevent CSV/Formula Injection attacks.
     * Prefixes unsafe leading chars (=, +, -, @, tab, cr) with a single quote.
     */
    fun sanitizeForSpreadsheet(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return ""
        val firstChar = trimmed[0]
        val isPotentiallyFormula = firstChar == '=' || firstChar == '+' || 
                firstChar == '-' || firstChar == '@' || firstChar == '\t' || firstChar == '\r'

        var escaped = trimmed.replace("\"", "\"\"")
        if (isPotentiallyFormula) {
            escaped = "'$escaped"
        }
        return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || isPotentiallyFormula) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
