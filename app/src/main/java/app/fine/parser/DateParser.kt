package app.fine.parser

import java.text.Normalizer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.temporal.ChronoField
import java.util.Locale

object DateParser {

    private val locale = Locale("fr", "FR")
    private val zoneId: ZoneId = ZoneId.of("Africa/Casablanca")
    private val whitespaceRegex = Regex("\\s+")

    private val weekdays = mapOf(
        "lundi" to DayOfWeek.MONDAY,
        "mardi" to DayOfWeek.TUESDAY,
        "mercredi" to DayOfWeek.WEDNESDAY,
        "jeudi" to DayOfWeek.THURSDAY,
        "vendredi" to DayOfWeek.FRIDAY,
        "samedi" to DayOfWeek.SATURDAY,
        "dimanche" to DayOfWeek.SUNDAY
    )

    private val absoluteFormatters: List<DateTimeFormatter> = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("d/M/uuuu")
            .toFormatter(locale)
            .withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("d/M/")
            .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
            .toFormatter(locale)
            .withResolverStyle(ResolverStyle.STRICT)
    )

    fun parse(
        input: String,
        referenceDate: LocalDate = LocalDate.now(zoneId)
    ): Result<LocalDate> = runCatching {
        val normalized = normalize(input)
        if (normalized.isBlank()) {
            error("Date non reconnue.")
        }

        parseAbsolute(normalized)?.let { return@runCatching it }
        parseRelative(normalized, referenceDate)?.let { return@runCatching it }
        parseWeekday(normalized, referenceDate)?.let { return@runCatching it }
        parseDayOfMonth(normalized, referenceDate)?.let { return@runCatching it }

        error("Date non reconnue.")
    }

    private fun parseAbsolute(normalized: String): LocalDate? =
        absoluteFormatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDate.parse(normalized, formatter) }.getOrNull()
        }

    private fun parseRelative(normalized: String, referenceDate: LocalDate): LocalDate? {
        if (normalized.contains("aujourd'hui") || normalized.contains("aujourdhui")) {
            return referenceDate
        }
        if (normalized.contains("hier")) {
            return referenceDate.minusDays(1)
        }
        return null
    }

    private fun parseWeekday(normalized: String, referenceDate: LocalDate): LocalDate? {
        val dayOfWeek = weekdays.entries.firstOrNull { (label, _) ->
            normalized.contains(label)
        }?.value ?: return null

        var candidate = referenceDate
        val target = dayOfWeek.value
        val reference = referenceDate.dayOfWeek.value

        if (target == reference) {
            return candidate
        }

        while (candidate.dayOfWeek != dayOfWeek) {
            candidate = candidate.minusDays(1)
        }
        return candidate
    }

    private fun parseDayOfMonth(normalized: String, referenceDate: LocalDate): LocalDate? {
        val numberMatch = Regex("(\\d{1,2})").find(normalized) ?: return null
        val day = numberMatch.value.toInt()
        if (day !in 1..31) return null

        var candidate = referenceDate
        repeat(14) {
            val length = candidate.lengthOfMonth()
            if (day <= length) {
                if (candidate.month == referenceDate.month && candidate.year == referenceDate.year) {
                    if (day <= referenceDate.dayOfMonth) {
                        return candidate.withDayOfMonth(day)
                    }
                } else {
                    return candidate.withDayOfMonth(day)
                }
            }
            candidate = candidate.minusMonths(1)
        }
        return null
    }

    private fun normalize(value: String): String {
        val nfkc = Normalizer.normalize(value, Normalizer.Form.NFKC)
        val base = nfkc
            .lowercase(locale)
            .replace('’', '\'')
            .replace(whitespaceRegex, " ")
            .trim()
        return if (base.startsWith("le ")) base.removePrefix("le ").trim() else base
    }
}
