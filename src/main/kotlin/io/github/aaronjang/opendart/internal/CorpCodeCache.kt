package io.github.aaronjang.opendart.internal

import io.github.aaronjang.opendart.model.CorpCode
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

class CorpCodeCache(
    private val client: DartClient,
    private val cacheDir: Path = Path.of("docs_cache"),
) {
    private var corpCodes: List<CorpCode> = emptyList()

    suspend fun load(): List<CorpCode> {
        if (corpCodes.isNotEmpty()) return corpCodes

        Files.createDirectories(cacheDir)
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val cacheFile = cacheDir.resolve("opendartreader_corp_codes_$today.dat")

        // Clean old cache files
        cacheDir.toFile().listFiles()?.filter {
            it.name.startsWith("opendartreader_corp_codes_") && it.name != cacheFile.fileName.toString()
        }?.forEach { it.delete() }

        if (Files.exists(cacheFile)) {
            val xml = Files.readString(cacheFile)
            corpCodes = parseCorpCodeXml(xml)
        } else {
            val bytes = client.getBytes("corpCode.xml", emptyMap())
            val xml = extractXmlFromZip(bytes)
            Files.writeString(cacheFile, xml)
            corpCodes = parseCorpCodeXml(xml)
        }
        return corpCodes
    }

    fun findCorpCode(corp: String): String? {
        if (corp.all { it.isDigit() } && corp.length == 6) {
            return corpCodes.firstOrNull { it.stockCode.trim() == corp }?.corpCode
        }
        if (corp.all { it.isDigit() }) {
            return corpCodes.firstOrNull { it.corpCode == corp }?.corpCode
        }
        return corpCodes.firstOrNull { it.corpName == corp }?.corpCode
    }

    fun findByName(name: String): List<CorpCode> {
        return corpCodes.filter { it.corpName.contains(name) }
    }

    private fun extractXmlFromZip(bytes: ByteArray): String {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            val entry = zis.nextEntry
            if (entry != null) {
                return zis.readBytes().toString(Charsets.UTF_8)
            }
        }
        throw IllegalStateException("Empty zip file")
    }
}

fun parseCorpCodeXml(xml: String): List<CorpCode> {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val doc = builder.parse(xml.byteInputStream())
    val nodeList = doc.getElementsByTagName("list")

    val result = mutableListOf<CorpCode>()
    for (i in 0 until nodeList.length) {
        val node = nodeList.item(i)
        val children = node.childNodes
        var corpCode = ""
        var corpName = ""
        var stockCode = ""
        for (j in 0 until children.length) {
            val child = children.item(j)
            when (child.nodeName) {
                "corp_code" -> corpCode = child.textContent ?: ""
                "corp_name" -> corpName = child.textContent ?: ""
                "stock_code" -> stockCode = child.textContent ?: ""
            }
        }
        result.add(CorpCode(corpCode, corpName, stockCode))
    }
    return result
}
