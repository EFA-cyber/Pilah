package id.pilah.feature.foldering

/** Menentukan akar volume penyimpanan (internal/SD card) dari path absolut sebuah file. */
object VolumeRootResolver {

    fun resolve(path: String): String {
        val segments = path.removePrefix("/").split("/")
        return when {
            segments.size >= 3 && segments[0] == "storage" && segments[1] == "emulated" ->
                "/storage/emulated/${segments[2]}"
            segments.size >= 2 && segments[0] == "storage" ->
                "/storage/${segments[1]}"
            else -> DEFAULT_ROOT
        }
    }

    private const val DEFAULT_ROOT = "/storage/emulated/0"
}
