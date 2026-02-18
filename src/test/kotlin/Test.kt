
import cn.luorenmu.action.request.HTTPRequestUtil
import io.ktor.client.call.*
import kotlinx.coroutines.runBlocking
import java.util.zip.GZIPInputStream

/**
 *
 * @author LoMu
 * Date 2026/2/2 08:17
 */

private val bilibiliBVID = "BV1[0-9a-zA-Z]{9}"

private val bilibiliVideoShortLink = "((https://bili2233.cn/([a-zA-Z0-9]+))|(https://b23.tv/([a-zA-Z0-9]+)))"
private fun findBilibiliLinkBvid(message: String): String? {
    val bvidRegex = bilibiliBVID.toRegex()
    if (message.contains(bvidRegex)) {
        return bvidRegex.find(message)?.groupValues[0]?.let { bvid ->
            if (bvid.length == 12) {
                return bvid
            }
            null
        }

    }
    val shortRegex = bilibiliVideoShortLink.toRegex()
    if (message.contains(shortRegex)) {
        // short link to long link
        val shortLink = shortRegex.find(message)!!.groupValues[1]
        val bytes = runBlocking { HTTPRequestUtil.call(shortLink).body<ByteArray>() }
        val decompressed = GZIPInputStream(bytes.inputStream()).readBytes()
        val text = decompressed.toString(Charsets.UTF_8)
        return bvidRegex.find(text)?.groupValues[0]
    }
    return null
}

suspend fun main() {
}
