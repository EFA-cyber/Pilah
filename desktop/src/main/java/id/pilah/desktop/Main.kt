package id.pilah.desktop

import id.pilah.cli.FileClassification
import id.pilah.cli.FileScanner
import id.pilah.cli.FolderingPlanner
import id.pilah.core.common.FileMover
import id.pilah.core.model.FileCategory
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.UIManager

fun main() {
    runCatching { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) }
    SwingUtilities.invokeLater { PilahWindow().isVisible = true }
}

/** Jendela utama aplikasi desktop Pilah: pilih folder, lalu Pindai atau Rapikan Sekarang. */
private class PilahWindow : JFrame("Pilah - Rapikan File") {

    private var selectedFolder: File? = null

    private val folderField = JTextField().apply { isEditable = false }
    private val logArea = JTextArea(
        "Pilih folder, lalu klik \"Pindai\" untuk melihat ringkasan,\n" +
            "atau \"Rapikan Sekarang\" untuk memindahkan file ke subfolder kategori.",
    ).apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
    }
    private val scanButton = JButton("Pindai").apply { addActionListener { runScan() } }
    private val rapikanButton = JButton("Rapikan Sekarang").apply { addActionListener { runRapikan() } }

    init {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        size = Dimension(640, 460)
        minimumSize = Dimension(480, 360)
        setLocationRelativeTo(null)

        val pickButton = JButton("Pilih Folder...").apply { addActionListener { pickFolder() } }

        val folderPanel = JPanel(BorderLayout(8, 0)).apply {
            add(JLabel("Folder:"), BorderLayout.WEST)
            add(folderField, BorderLayout.CENTER)
            add(pickButton, BorderLayout.EAST)
        }

        val actionPanel = JPanel().apply {
            add(scanButton)
            add(rapikanButton)
        }

        val topPanel = JPanel(BorderLayout(8, 8)).apply {
            add(folderPanel, BorderLayout.NORTH)
            add(actionPanel, BorderLayout.SOUTH)
        }

        contentPane = JPanel(BorderLayout(8, 8)).apply {
            border = BorderFactory.createEmptyBorder(12, 12, 12, 12)
            add(topPanel, BorderLayout.NORTH)
            add(JScrollPane(logArea), BorderLayout.CENTER)
        }
    }

    private fun pickFolder() {
        val chooser = JFileChooser().apply { fileSelectionMode = JFileChooser.DIRECTORIES_ONLY }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFolder = chooser.selectedFile
            folderField.text = selectedFolder?.path
            log("Folder dipilih: ${selectedFolder?.path}")
        }
    }

    private fun runScan() {
        val folder = selectedFolder ?: return showSelectFolderWarning()

        setBusy(true)
        log("Memindai ${folder.path}...")

        Thread {
            val files = FileScanner.scan(folder)
            val counts = FileClassification.classify(files).values
                .groupingBy { it.category }
                .eachCount()

            SwingUtilities.invokeLater {
                log("Ditemukan ${files.size} file.")
                log("Penting: ${counts[FileCategory.PENTING] ?: 0}")
                log("Layak Dihapus: ${counts[FileCategory.LAYAK_DIHAPUS] ?: 0}")
                log("Ambigu: ${counts[FileCategory.AMBIGU] ?: 0}")
                setBusy(false)
            }
        }.start()
    }

    private fun runRapikan() {
        val folder = selectedFolder ?: return showSelectFolderWarning()

        val confirmed = JOptionPane.showConfirmDialog(
            this,
            "File akan dipindahkan ke subfolder kategori (Karantina, Dokumen Penting,\n" +
                "Arsip, dll.) di dalam:\n${folder.path}\n\nLanjutkan?",
            "Konfirmasi Rapikan",
            JOptionPane.YES_NO_OPTION,
        )
        if (confirmed != JOptionPane.YES_OPTION) return

        setBusy(true)
        log("Merapikan ${folder.path}...")

        Thread {
            val files = FileScanner.scan(folder)
            val categories = FileClassification.classify(files).mapValues { it.value.category }
            val plan = FolderingPlanner.plan(folder, files, categories)

            val results = if (plan.isEmpty()) {
                listOf("Tidak ada file yang perlu dipindahkan.")
            } else {
                plan.groupBy { it.targetFolder }
                    .toSortedMap()
                    .map { (target, items) ->
                        val moved = items.count { FileMover.move(File(it.file.path), File(it.targetPath)) }
                        "$moved file dipindahkan ke $target/"
                    }
            }

            SwingUtilities.invokeLater {
                log("Selesai memindai ${files.size} file.")
                results.forEach { log(it) }
                setBusy(false)
            }
        }.start()
    }

    private fun showSelectFolderWarning() {
        JOptionPane.showMessageDialog(this, "Pilih folder terlebih dahulu.")
    }

    private fun setBusy(busy: Boolean) {
        scanButton.isEnabled = !busy
        rapikanButton.isEnabled = !busy
    }

    private fun log(message: String) {
        logArea.append("\n$message")
    }
}
