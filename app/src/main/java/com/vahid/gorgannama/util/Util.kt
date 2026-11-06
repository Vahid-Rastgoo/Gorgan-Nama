package com.vahid.gorgannama.util

import android.content.Context
import android.net.Uri
import com.vahid.gorgannama.data.Business
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import java.util.UUID

object Time {
    private val zone: ZoneId = ZoneId.of("Asia/Tehran")
    fun today(): Long = LocalDate.now(zone).toEpochDay()
    fun now(): LocalDateTime = LocalDateTime.now(zone)
}

object Fa {
    /** Persian week order, values are java.time.DayOfWeek numbers */
    val WEEK: List<Pair<Int, String>> = listOf(
        6 to "شنبه", 7 to "یکشنبه", 1 to "دوشنبه", 2 to "سه‌شنبه",
        3 to "چهارشنبه", 4 to "پنجشنبه", 5 to "جمعه"
    )
    private val MONTHS = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    fun digits(s: String): String = buildString(s.length) {
        for (c in s) append(if (c in '0'..'9') '۰' + (c - '0') else c)
    }

    fun latin(s: String): String = buildString(s.length) {
        for (c in s) {
            append(
                when (c) {
                    in '۰'..'۹' -> '0' + (c - '۰')
                    in '٠'..'٩' -> '0' + (c - '٠')
                    else -> c
                }
            )
        }
    }

    fun num(n: Int): String = digits(n.toString())
    fun num(n: Long): String = digits(n.toString())

    fun money(v: Long): String =
        digits(String.format(Locale.US, "%,d", v).replace(',', '٬')) + " تومان"

    fun jalali(epochDay: Long): IntArray {
        val d = LocalDate.ofEpochDay(epochDay)
        return g2j(d.year, d.monthValue, d.dayOfMonth)
    }

    fun date(epochDay: Long): String {
        val j = jalali(epochDay)
        return digits("${j[0]}/${pad(j[1])}/${pad(j[2])}")
    }

    fun longDate(epochDay: Long): String {
        val j = jalali(epochDay)
        return digits(j[2].toString()) + " " + MONTHS[j[1] - 1] + " " + digits(j[0].toString())
    }

    fun fileDate(epochDay: Long): String {
        val j = jalali(epochDay)
        return "${j[0]}-${pad(j[1])}-${pad(j[2])}"
    }

    fun rel(epochDay: Long, today: Long): String {
        val d = epochDay - today
        return when {
            d == 0L -> "امروز"
            d == 1L -> "فردا"
            d == -1L -> "دیروز"
            d > 1L -> "${num(d)} روز دیگر"
            else -> "${num(-d)} روز پیش"
        }
    }

    fun normalize(s: String): String = latin(s).lowercase(Locale.ROOT)
        .replace('ي', 'ی').replace('ى', 'ی').replace('ك', 'ک').replace('ة', 'ه')
        .replace("\u200c", "").replace("ـ", "").trim()

    private fun pad(n: Int): String = if (n < 10) "0$n" else n.toString()

    /** Gregorian to Jalali (jdf algorithm). */
    private fun g2j(gy: Int, gm: Int, gd: Int): IntArray {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 + gd + gdm[gm - 1]
        var jy = -1595 + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
        val jd = 1 + if (days < 186) days % 31 else (days - 186) % 30
        return intArrayOf(jy, jm, jd)
    }
}

object Hours {
    data class Seg(val start: Int, val end: Int)

    private val re = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(?:-|–|تا)\\s*(\\d{1,2})(?::(\\d{2}))?")

    fun parse(raw: String): List<Seg> {
        val s = Fa.latin(raw).trim()
        if (s.isBlank()) return emptyList()
        if (s == "24" || s.contains("24 ساعت") || s.contains("شبانه")) return listOf(Seg(0, 1440))
        return re.findAll(s).mapNotNull { m ->
            val h1 = m.groupValues[1].toInt()
            val m1 = m.groupValues[2].ifBlank { "0" }.toInt()
            val h2 = m.groupValues[3].toInt()
            val m2 = m.groupValues[4].ifBlank { "0" }.toInt()
            if (h1 > 24 || h2 > 24 || m1 > 59 || m2 > 59) null else Seg(h1 * 60 + m1, h2 * 60 + m2)
        }.toList()
    }

    private fun inSeg(s: Seg, t: Int): Boolean =
        if (s.end > s.start) t >= s.start && t < s.end else t >= s.start || t < s.end

    /** null = hours unknown */
    fun isOpen(b: Business, now: LocalDateTime = Time.now()): Boolean? {
        val segs = parse(b.hours)
        if (segs.isEmpty()) return null
        if (now.dayOfWeek.value in b.closedDays) return false
        val t = now.hour * 60 + now.minute
        return segs.any { inSeg(it, t) }
    }

    fun statusText(b: Business): String {
        val now = Time.now()
        return when (isOpen(b, now)) {
            null -> "ساعت کاری ثبت نشده"
            false -> if (now.dayOfWeek.value in b.closedDays) "امروز تعطیل است" else "الان بسته است"
            true -> {
                val t = now.hour * 60 + now.minute
                val seg = parse(b.hours).firstOrNull { inSeg(it, t) }
                if (seg == null || (seg.start == 0 && seg.end == 1440)) "الان باز است"
                else "الان باز است · تا ${fmt(seg.end)}"
            }
        }
    }

    fun closedText(b: Business): String =
        if (b.closedDays.isEmpty()) "" else "تعطیل: " + Fa.WEEK.filter { it.first in b.closedDays }.joinToString("، ") { it.second }

    private fun fmt(min: Int): String {
        val m = min % 1440
        return Fa.digits(String.format(Locale.US, "%02d:%02d", m / 60, m % 60))
    }
}

object Geo {
    private val re = Regex("(-?\\d{1,2}\\.\\d{3,})\\s*[,،/ ]\\s*(-?\\d{1,3}\\.\\d{3,})")

    fun parse(s: String): Pair<Double, Double>? {
        val m = re.find(Fa.latin(s)) ?: return null
        val lat = m.groupValues[1].toDoubleOrNull() ?: return null
        val lng = m.groupValues[2].toDoubleOrNull() ?: return null
        return if (lat in -90.0..90.0 && lng in -180.0..180.0) lat to lng else null
    }
}

object Files {
    fun copyImage(ctx: Context, uri: Uri): String? = try {
        val dir = File(ctx.filesDir, "images").apply { mkdirs() }
        val f = File(dir, UUID.randomUUID().toString() + ".jpg")
        val input = ctx.contentResolver.openInputStream(uri)
        if (input == null) null else {
            input.use { i -> f.outputStream().use { o -> i.copyTo(o) } }
            f.absolutePath
        }
    } catch (e: Exception) {
        null
    }

    fun write(ctx: Context, uri: Uri, text: String): Boolean = try {
        val os = ctx.contentResolver.openOutputStream(uri, "wt")
        if (os == null) false else {
            os.use { it.write(text.toByteArray(Charsets.UTF_8)) }
            true
        }
    } catch (e: Exception) {
        false
    }

    fun read(ctx: Context, uri: Uri): String? = try {
        ctx.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
    } catch (e: Exception) {
        null
    }
}
