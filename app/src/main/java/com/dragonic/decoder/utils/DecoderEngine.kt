package com.dragonic.decoder.utils

import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.math.ln

object DecoderEngine {

    enum class DecoderType(val label: String) {
        BASE64("Base64"),
        BASE32("Base32"),
        BASE16("Base16 (Hex)"),
        HEX("Hex"),
        URL("URL"),
        JWT("JWT"),
        UNICODE("Unicode"),
        HTML_ENTITY("HTML Entity"),
        BINARY("Binary"),
        OCTAL("Octal"),
        ROT13("ROT13"),
        CAESAR("Caesar Cipher")
    }

    data class DecodeResult(
        val output: String,
        val success: Boolean,
        val errorMessage: String = "",
        val durationMs: Long = 0L,
        val charCount: Int = 0
    )

    fun decode(input: String, type: DecoderType, shift: Int = 13): DecodeResult {
        val start = System.currentTimeMillis()
        return try {
            val result = when (type) {
                DecoderType.BASE64    -> decodeBase64(input)
                DecoderType.BASE32    -> decodeBase32(input)
                DecoderType.BASE16    -> decodeHex(input)
                DecoderType.HEX      -> decodeHex(input)
                DecoderType.URL      -> decodeUrl(input)
                DecoderType.JWT      -> decodeJwt(input)
                DecoderType.UNICODE  -> decodeUnicode(input)
                DecoderType.HTML_ENTITY -> decodeHtmlEntity(input)
                DecoderType.BINARY   -> decodeBinary(input)
                DecoderType.OCTAL    -> decodeOctal(input)
                DecoderType.ROT13    -> rot13(input)
                DecoderType.CAESAR   -> caesarDecode(input, shift)
            }
            val elapsed = System.currentTimeMillis() - start
            DecodeResult(result, true, durationMs = elapsed, charCount = result.length)
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - start
            DecodeResult("", false, e.message ?: "Unknown error", elapsed)
        }
    }

    fun encode(input: String, type: DecoderType, shift: Int = 13): DecodeResult {
        val start = System.currentTimeMillis()
        return try {
            val result = when (type) {
                DecoderType.BASE64    -> encodeBase64(input)
                DecoderType.BASE32    -> encodeBase32(input)
                DecoderType.BASE16    -> encodeHex(input)
                DecoderType.HEX      -> encodeHex(input)
                DecoderType.URL      -> encodeUrl(input)
                DecoderType.JWT      -> "JWT encoding requires header, payload, secret"
                DecoderType.UNICODE  -> encodeUnicode(input)
                DecoderType.HTML_ENTITY -> encodeHtmlEntity(input)
                DecoderType.BINARY   -> encodeBinary(input)
                DecoderType.OCTAL    -> encodeOctal(input)
                DecoderType.ROT13    -> rot13(input)
                DecoderType.CAESAR   -> caesarEncode(input, shift)
            }
            val elapsed = System.currentTimeMillis() - start
            DecodeResult(result, true, durationMs = elapsed, charCount = result.length)
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - start
            DecodeResult("", false, e.message ?: "Unknown error", elapsed)
        }
    }

    // ---------- BASE64 ----------
    private fun decodeBase64(input: String): String {
        val cleaned = input.trim()
        val bytes = Base64.decode(cleaned, Base64.DEFAULT)
        return String(bytes, Charsets.UTF_8)
    }

    private fun encodeBase64(input: String): String =
        Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

    // ---------- BASE32 ----------
    private val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    private fun decodeBase32(input: String): String {
        val cleaned = input.trim().uppercase().trimEnd('=')
        var buffer = 0
        var bitsLeft = 0
        val result = StringBuilder()
        for (c in cleaned) {
            val idx = BASE32_CHARS.indexOf(c)
            if (idx < 0) continue
            buffer = (buffer shl 5) or idx
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                result.append((buffer shr bitsLeft and 0xFF).toChar())
            }
        }
        return result.toString()
    }

    private fun encodeBase32(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val result = StringBuilder()
        var buffer = 0
        var bitsLeft = 0
        for (b in bytes) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                bitsLeft -= 5
                result.append(BASE32_CHARS[(buffer shr bitsLeft) and 0x1F])
            }
        }
        if (bitsLeft > 0) {
            buffer = buffer shl (5 - bitsLeft)
            result.append(BASE32_CHARS[buffer and 0x1F])
        }
        while (result.length % 8 != 0) result.append('=')
        return result.toString()
    }

    // ---------- HEX / BASE16 ----------
    private fun decodeHex(input: String): String {
        val cleaned = input.trim().replace("\\s+".toRegex(), "").replace("0x", "").replace(":", "")
        require(cleaned.length % 2 == 0) { "Invalid hex length" }
        val bytes = ByteArray(cleaned.length / 2) { i ->
            cleaned.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return String(bytes, Charsets.UTF_8)
    }

    private fun encodeHex(input: String): String =
        input.toByteArray(Charsets.UTF_8).joinToString("") { "%02X".format(it) }

    // ---------- URL ----------
    private fun decodeUrl(input: String): String = URLDecoder.decode(input.trim(), "UTF-8")

    private fun encodeUrl(input: String): String = URLEncoder.encode(input.trim(), "UTF-8")

    // ---------- JWT ----------
    private fun decodeJwt(input: String): String {
        val parts = input.trim().split(".")
        require(parts.size >= 2) { "Invalid JWT format (expected header.payload.signature)" }
        val header = decodeBase64Url(parts[0])
        val payload = decodeBase64Url(parts[1])
        val sig = if (parts.size > 2) parts[2] else ""
        return buildString {
            appendLine("=== HEADER ===")
            appendLine(prettyJson(header))
            appendLine()
            appendLine("=== PAYLOAD ===")
            appendLine(prettyJson(payload))
            appendLine()
            appendLine("=== SIGNATURE ===")
            append(if (sig.isEmpty()) "(none)" else sig)
        }
    }

    private fun decodeBase64Url(input: String): String {
        var cleaned = input.replace('-', '+').replace('_', '/')
        while (cleaned.length % 4 != 0) cleaned += "="
        return String(Base64.decode(cleaned, Base64.DEFAULT), Charsets.UTF_8)
    }

    private fun prettyJson(json: String): String {
        val sb = StringBuilder()
        var indent = 0
        var inString = false
        for (ch in json) {
            when {
                ch == '"' -> { inString = !inString; sb.append(ch) }
                inString  -> sb.append(ch)
                ch == '{' || ch == '[' -> { sb.append(ch); sb.append('\n'); indent++; repeat(indent) { sb.append("  ") } }
                ch == '}' || ch == ']' -> { sb.append('\n'); indent--; repeat(indent) { sb.append("  ") }; sb.append(ch) }
                ch == ',' -> { sb.append(ch); sb.append('\n'); repeat(indent) { sb.append("  ") } }
                ch == ':' -> { sb.append(": ") }
                ch == ' ' -> { /* skip */ }
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    // ---------- UNICODE ----------
    private fun decodeUnicode(input: String): String {
        return input.trim()
            .replace(Regex("""\\u([0-9a-fA-F]{4})""")) { m ->
                m.groupValues[1].toInt(16).toChar().toString()
            }
            .replace(Regex("""U\+([0-9a-fA-F]{4,6})""")) { m ->
                m.groupValues[1].toInt(16).toChar().toString()
            }
    }

    private fun encodeUnicode(input: String): String =
        input.map { "\\u%04X".format(it.code) }.joinToString("")

    // ---------- HTML ENTITY ----------
    private fun decodeHtmlEntity(input: String): String {
        return input
            .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
            .replace("&quot;", "\"").replace("&#39;", "'").replace("&apos;", "'")
            .replace("&nbsp;", " ").replace("&copy;", "©").replace("&reg;", "®")
            .replace("&trade;", "™").replace("&mdash;", "—").replace("&ndash;", "–")
            .replace(Regex("&#(\\d+);")) { m -> m.groupValues[1].toInt().toChar().toString() }
            .replace(Regex("&#x([0-9a-fA-F]+);")) { m -> m.groupValues[1].toInt(16).toChar().toString() }
    }

    private fun encodeHtmlEntity(input: String): String =
        input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#39;")

    // ---------- BINARY ----------
    private fun decodeBinary(input: String): String {
        val tokens = input.trim().split("\\s+".toRegex())
        require(tokens.all { it.matches(Regex("[01]+")) }) { "Input contains non-binary characters" }
        val result = StringBuilder()
        for (token in tokens) {
            val padded = token.padStart(8, '0')
            result.append(padded.toInt(2).toChar())
        }
        return result.toString()
    }

    private fun encodeBinary(input: String): String =
        input.toByteArray(Charsets.UTF_8).joinToString(" ") {
            Integer.toBinaryString(it.toInt() and 0xFF).padStart(8, '0')
        }

    // ---------- OCTAL ----------
    private fun decodeOctal(input: String): String {
        val tokens = input.trim().split("\\s+".toRegex())
        return tokens.joinToString("") { token ->
            token.toInt(8).toChar().toString()
        }
    }

    private fun encodeOctal(input: String): String =
        input.toByteArray(Charsets.UTF_8).joinToString(" ") {
            Integer.toOctalString(it.toInt() and 0xFF).padStart(3, '0')
        }

    // ---------- ROT13 ----------
    private fun rot13(input: String): String = input.map { c ->
        when {
            c in 'a'..'z' -> 'a' + (c - 'a' + 13) % 26
            c in 'A'..'Z' -> 'A' + (c - 'A' + 13) % 26
            else -> c
        }
    }.joinToString("")

    // ---------- CAESAR ----------
    private fun caesarDecode(input: String, shift: Int): String = caesarShift(input, -shift)
    private fun caesarEncode(input: String, shift: Int): String = caesarShift(input, shift)

    private fun caesarShift(input: String, shift: Int): String = input.map { c ->
        when {
            c in 'a'..'z' -> 'a' + ((c - 'a' + shift + 26) % 26)
            c in 'A'..'Z' -> 'A' + ((c - 'A' + shift + 26) % 26)
            else -> c
        }
    }.joinToString("")

    // ---------- AUTO DETECT ----------
    fun detectEncoding(input: String): String {
        val trimmed = input.trim()
        return when {
            isBase64(trimmed)   -> "Base64"
            isHex(trimmed)      -> "Hex / Base16"
            isBase32(trimmed)   -> "Base32"
            isBinary(trimmed)   -> "Binary"
            isOctal(trimmed)    -> "Octal"
            isUrl(trimmed)      -> "URL Encoded"
            isJwt(trimmed)      -> "JWT"
            isUnicode(trimmed)  -> "Unicode Escape"
            isHtmlEntity(trimmed) -> "HTML Entity"
            else                -> "Unknown / Plain Text"
        }
    }

    private fun isBase64(s: String) = s.matches(Regex("^[A-Za-z0-9+/]+=*$")) && s.length % 4 == 0
    private fun isHex(s: String) = s.replace("\\s".toRegex(), "").matches(Regex("^[0-9A-Fa-f]+$")) && s.replace("\\s".toRegex(), "").length % 2 == 0
    private fun isBase32(s: String) = s.matches(Regex("^[A-Z2-7]+=*$"))
    private fun isBinary(s: String) = s.trim().split("\\s+".toRegex()).all { it.matches(Regex("[01]{8}")) }
    private fun isOctal(s: String) = s.trim().split("\\s+".toRegex()).all { it.matches(Regex("[0-7]{3}")) }
    private fun isUrl(s: String) = s.contains("%[0-9A-Fa-f]{2}".toRegex())
    private fun isJwt(s: String) = s.split(".").size == 3 && s.split(".").all { it.isNotEmpty() }
    private fun isUnicode(s: String) = s.contains("\\\\u[0-9a-fA-F]{4}".toRegex())
    private fun isHtmlEntity(s: String) = s.contains("&[a-zA-Z]+;".toRegex()) || s.contains("&#[0-9]+;".toRegex())

    fun calculateEntropy(input: String): Double {
        if (input.isEmpty()) return 0.0
        val freq = input.groupBy { it }.mapValues { it.value.size.toDouble() / input.length }
        return -freq.values.sumOf { p -> if (p > 0) p * ln(p) / ln(2.0) else 0.0 }
    }
}
