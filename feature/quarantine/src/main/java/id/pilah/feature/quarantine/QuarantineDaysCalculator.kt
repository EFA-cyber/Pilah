package id.pilah.feature.quarantine

import java.time.Duration
import java.time.Instant

/** Menghitung sisa hari sebelum entri Karantina dihapus permanen (purge otomatis 30 hari). */
object QuarantineDaysCalculator {

    fun daysRemaining(now: Instant, purgeAfter: Instant): Long =
        Duration.between(now, purgeAfter).toDays().coerceAtLeast(0)
}
