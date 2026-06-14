package id.pilah.cli

import id.pilah.core.common.FileMover
import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem
import java.io.File
import java.time.Instant

fun main(args: Array<String>) {
    val command = args.getOrNull(0)
    val path = args.getOrNull(1)
    if (command == null || path == null) {
        printUsage()
        return
    }

    val root = File(path)
    if (!root.isDirectory) {
        println("Folder tidak ditemukan: ${root.path}")
        return
    }

    when (command) {
        "scan" -> scan(root)
        "rapikan" -> rapikan(root)
        else -> printUsage()
    }
}

private fun printUsage() {
    println("Penggunaan: pilah-cli <scan|rapikan> <folder>")
}

private fun scan(root: File) {
    val files = FileScanner.scan(root)
    println("Memindai ${files.size} file...")

    val categories = classify(files).mapValues { it.value.category }
    val counts = categories.values.groupingBy { it }.eachCount()

    println("Penting: ${counts[FileCategory.PENTING] ?: 0}")
    println("Layak Dihapus: ${counts[FileCategory.LAYAK_DIHAPUS] ?: 0}")
    println("Ambigu: ${counts[FileCategory.AMBIGU] ?: 0}")
}

private fun rapikan(root: File) {
    val files = FileScanner.scan(root)
    println("Memindai ${files.size} file...")

    val categories = classify(files).mapValues { it.value.category }
    val plan = FolderingPlanner.plan(root, files, categories)

    if (plan.isEmpty()) {
        println("Tidak ada file yang perlu dipindahkan.")
        return
    }

    plan.groupBy { it.targetFolder }
        .toSortedMap()
        .forEach { (folder, items) ->
            val moved = items.count { FileMover.move(File(it.file.path), File(it.targetPath)) }
            println("$moved file dipindahkan ke $folder/")
        }
}

private fun classify(files: List<FileItem>): Map<String, ClassificationResult> {
    val context = RuleContext(now = Instant.now(), duplicateOfOriginalName = findDuplicates(files))
    val weights = RuleWeights()
    return files.associate { it.path to Classifier.classify(it, context, weights) }
}

/** Kelompokkan file berdasarkan hash SHA-256; selain file tertua per kelompok dianggap duplikat. */
private fun findDuplicates(files: List<FileItem>): Map<String, String> {
    val duplicates = mutableMapOf<String, String>()
    files
        .mapNotNull { file -> FileHasher.hash(File(file.path))?.let { hash -> hash to file } }
        .groupBy({ it.first }, { it.second })
        .values
        .filter { it.size > 1 }
        .forEach { group ->
            val original = group.minByOrNull { it.createdAt } ?: return@forEach
            group.filter { it.path != original.path }.forEach { duplicate ->
                duplicates[duplicate.path] = original.name
            }
        }
    return duplicates
}
