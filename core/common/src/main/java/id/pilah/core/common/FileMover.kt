package id.pilah.core.common

import java.io.File
import java.io.IOException

/** Memindahkan file ke lokasi baru, membuat folder induk tujuan bila perlu (Fase 4 & Karantina). */
object FileMover {

    fun move(from: File, to: File): Boolean {
        if (!from.exists()) return false
        to.parentFile?.mkdirs()
        if (from.renameTo(to)) return true
        return try {
            from.copyTo(to, overwrite = true)
            from.delete()
        } catch (e: IOException) {
            false
        }
    }
}
