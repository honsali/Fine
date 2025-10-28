package app.fine.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object MoneyFormatter {
    private val locale: Locale = Locale.forLanguageTag("fr-FR")
    private val symbols = DecimalFormatSymbols(locale).apply {
        decimalSeparator = ','
        groupingSeparator = ' '
    }
    private val decimalFormat = DecimalFormat("#,##0.00", symbols)

    fun format(amountMinor: Long): String {
        val major = amountMinor / 100.0
        return "${decimalFormat.format(major)} MAD"
    }

    fun formatPlain(amountMinor: Long): String {
        val major = amountMinor / 100.0
        return decimalFormat.format(major)
    }
}
