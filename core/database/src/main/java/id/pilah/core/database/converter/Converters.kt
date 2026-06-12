package id.pilah.core.database.converter

import androidx.room.TypeConverter
import java.time.Instant

/** Konversi [Instant] <-> epoch milliseconds untuk kolom datetime di ERD. */
class Converters {

    @TypeConverter
    fun fromEpochMilli(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun toEpochMilli(instant: Instant?): Long? = instant?.toEpochMilli()
}
