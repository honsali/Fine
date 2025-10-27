package app.fine.parser

import java.text.Normalizer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
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
    private val accentRegex = Regex("\\p{Mn}+")

    private val weekdays = mapOf(
        "lundi" to DayOfWeek.MONDAY,
        "mardi" to DayOfWeek.TUESDAY,
        "mercredi" to DayOfWeek.WEDNESDAY,
        "jeudi" to DayOfWeek.THURSDAY,
        "vendredi" to DayOfWeek.FRIDAY,
        "samedi" to DayOfWeek.SATURDAY,
        "dimanche" to DayOfWeek.SUNDAY
    )

    private val monthNames = mapOf(
        "janvier" to 1,
        "fevrier" to 2,
        "mars" to 3,
        "avril" to 4,
        "mai" to 5,
        "juin" to 6,
        "juillet" to 7,
        "aout" to 8,
        "septembre" to 9,
        "octobre" to 10,
        "novembre" to 11,
        "decembre" to 12
    )

    private val dayMonthRegex =
        Regex("(\\d{1,2})\\s+(janvier|fevrier|mars|avril|mai|juin|juillet|aout|septembre|octobre|novembre|decembre)(?:\\s+(\\d{4}))?")

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
            .withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("d MMMM uuuu")
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
        parseDayMonthNamed(normalized, referenceDate)?.let { return@runCatching it }
        parseWeekday(normalized, referenceDate)?.let { return@runCatching it }
        parseDayOfMonth(normalized, referenceDate)?.let { return@runCatching it }

        error("Date non reconnue.")
    }

    private fun parseAbsolute(normalized: String): LocalDate? =
        absoluteFormatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDate.parse(normalized, formatter) }.getOrNull()
        }

    private fun parseRelative(normalized: String, referenceDate: LocalDate): LocalDate? {
        if (normalized.contains("aujourdhui")) {
            return referenceDate
        }
        if (normalized.contains("hier")) {
            return referenceDate.minusDays(1)
        }
        return null
    }

    private fun parseDayMonthNamed(normalized: String, referenceDate: LocalDate): LocalDate? {
        val match = dayMonthRegex.find(normalized) ?: return null
        val day = match.groupValues[1].toInt()
        val monthName = match.groupValues[2]
        val explicitYear = match.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.toIntOrNull()
        val month = monthNames[monthName] ?: return null

        fun buildDate(year: Int): LocalDate? {
            val yearMonth = YearMonth.of(year, month)
            if (day !in 1..yearMonth.lengthOfMonth()) return null
            return LocalDate.of(year, month, day)
        }

        val initialYear = explicitYear ?: referenceDate.year
        var candidate = buildDate(initialYear) ?: return null

        if (explicitYear == null && candidate.isAfter(referenceDate)) {
            candidate = buildDate(initialYear - 1) ?: candidate
        }

        return candidate
    }

    private fun parseWeekday(normalized: String, referenceDate: LocalDate): LocalDate? {
        val dayOfWeek = weekdays.entries.firstOrNull { (label, _) ->
            normalized.contains(label)
        }?.value ?: return null

        var candidate = referenceDate
        if (candidate.dayOfWeek == dayOfWeek) {
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
        val lower = nfkc
            .lowercase(locale)
            .replace('’', '\'')
            .replace(whitespaceRegex, " ")
            .trim()

        val withoutDiacritics = Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace(accentRegex, "")

        val trimmed = if (withoutDiacritics.startsWith("le ")) {
            withoutDiacritics.removePrefix("le ").trim()
        } else {
            withoutDiacritics
        }

        return trimmed
    }
}
