package app.fine.parser

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.Normalizer
import java.util.Locale

object AmountParser {

    private val locale: Locale = Locale.forLanguageTag("fr-FR")
    private val currencyRegex = Regex("\\b(dh?s?|dirhams?)\\b", RegexOption.IGNORE_CASE)
    private val allowedCharacters = Regex("[^\\d.,\\s]")
    private val whitespaceRegex = Regex("\\s+")

    fun parse(input: String): Result<Long> = runCatching {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFKC)
        val cleanedCurrency = currencyRegex.replace(normalized.lowercase(locale), " ")
        val strippedInvalid = cleanedCurrency.replace(allowedCharacters, " ")
        val trimmed = strippedInvalid.replace(whitespaceRegex, "")

        if (trimmed.isEmpty() || trimmed.all { !it.isDigit() }) {
            error("Montant non reconnu.")
        }

        val decimalNormalized = normalizeSeparators(trimmed)

        val decimal = BigDecimal(decimalNormalized)
            .setScale(2, RoundingMode.HALF_UP)

        decimal.movePointRight(2).longValueExact()
    }

    private fun normalizeSeparators(raw: String): String {
        val digitsOnly = raw.filter { it.isDigit() || it == '.' || it == ',' }
        val lastComma = digitsOnly.lastIndexOf(',')
        val lastDot = digitsOnly.lastIndexOf('.')

        val decimalSeparator: Char? = when {
            lastComma == -1 && lastDot == -1 -> null
            lastComma == -1 -> '.'
            lastDot == -1 -> ','
            lastComma > lastDot -> ','
            else -> '.'
        }

        val groupingSeparator: Char? = when (decimalSeparator) {
            null -> {
                if (digitsOnly.contains('.') && digitsOnly.contains(',')) {
                    error("Montant non reconnu.")
                } else null
            }
            ',' -> if (digitsOnly.contains('.')) '.' else null
            '.' -> if (digitsOnly.contains(',')) ',' else null
            else -> null
        }

        if (decimalSeparator != null && digitsOnly.count { it == decimalSeparator } > 1) {
            error("Montant non reconnu.")
        }

        val withoutGrouping = groupingSeparator?.let { digitsOnly.replace(it.toString(), "") } ?: digitsOnly
        val normalized = if (decimalSeparator != null) {
            withoutGrouping.replace(decimalSeparator, '.')
        } else {
            withoutGrouping
        }

        if (normalized.count { it == '.' } > 1) {
            error("Montant non reconnu.")
        }

        return normalized
    }
}
