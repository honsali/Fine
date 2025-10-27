package app.fine.parser

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.Normalizer
import java.util.Locale

object AmountParser {

    private val locale: Locale = Locale("fr", "FR")
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

        val decimalNormalized = when {
            trimmed.count { it == ',' } > 1 -> error("Montant non reconnu.")
            trimmed.count { it == '.' } > 1 -> error("Montant non reconnu.")
            trimmed.contains(',') && trimmed.contains('.') -> {
                // Assume comma is decimal separator when mixed.
                trimmed.replace(".", "").replace(',', '.')
            }
            trimmed.contains(',') -> trimmed.replace(',', '.')
            else -> trimmed
        }

        val decimal = BigDecimal(decimalNormalized)
            .setScale(2, RoundingMode.HALF_UP)

        decimal.movePointRight(2).longValueExact()
    }
}
