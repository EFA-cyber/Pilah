package id.pilah.feature.classification

import android.content.Context
import android.util.Xml
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import id.pilah.core.common.DispatcherProvider
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser

/** Ekstraksi cuplikan teks dari TXT (baca langsung), PDF (PdfBox-Android), dan DOCX (`word/document.xml`). */
class DefaultTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
) : TextExtractor {

    override fun supports(type: String): Boolean = type.lowercase() in SUPPORTED_TYPES

    override suspend fun extract(path: String, type: String, maxChars: Int): String? = withContext(dispatchers.io) {
        runCatching {
            when (type.lowercase()) {
                "txt" -> extractTxt(path, maxChars)
                "pdf" -> extractPdf(path, maxChars)
                "docx" -> extractDocx(path, maxChars)
                else -> null
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun extractTxt(path: String, maxChars: Int): String {
        val buffer = CharArray(maxChars)
        File(path).bufferedReader().use { reader ->
            val read = reader.read(buffer, 0, maxChars)
            return if (read <= 0) "" else String(buffer, 0, read)
        }
    }

    private fun extractPdf(path: String, maxChars: Int): String {
        PDFBoxResourceLoader.init(context)
        return PDDocument.load(File(path)).use { document ->
            val stripper = PDFTextStripper()
            stripper.startPage = 1
            stripper.endPage = minOf(document.numberOfPages, PDF_MAX_PAGES)
            stripper.getText(document).take(maxChars)
        }
    }

    private fun extractDocx(path: String, maxChars: Int): String {
        val text = StringBuilder()
        ZipFile(path).use { zip ->
            val entry = zip.getEntry(DOCX_DOCUMENT_ENTRY) ?: return ""
            zip.getInputStream(entry).use { input ->
                val parser = Xml.newPullParser()
                parser.setInput(input, "UTF-8")
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT && text.length < maxChars) {
                    if (eventType == XmlPullParser.TEXT) {
                        text.append(parser.text)
                        text.append(' ')
                    }
                    eventType = parser.next()
                }
            }
        }
        return text.toString().take(maxChars)
    }

    private companion object {
        val SUPPORTED_TYPES = setOf("txt", "pdf", "docx")
        const val PDF_MAX_PAGES = 5
        const val DOCX_DOCUMENT_ENTRY = "word/document.xml"
    }
}
