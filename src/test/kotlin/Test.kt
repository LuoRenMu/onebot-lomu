import cn.luorenmu.action.request.HTTPRequestUtil
import io.ktor.client.call.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.zip.GZIPInputStream

/**
 *
 * @author LoMu
 * Date 2025/11/12 14:31
 */


class Test

val link = "((https://bili2233.cn/([a-zA-Z0-9]+))|(https://b23.tv/([a-zA-Z0-9]+)))"
val bvid = "BV1[0-9a-zA-Z]{9}"

suspend fun main(args: Array<String>) {
    val regex = Regex("(${link}|${bvid})")
    println(regex.matches("https://b23.tv/kfbQjZZ"))
    println(findBilibiliLinkBvid("https://b23.tv/kfbQjZZ"))
}


private suspend fun findBilibiliLinkBvid(message: String): String? {
    val bvidRegex = bvid.toRegex()
    if (message.contains(bvidRegex)) {
        return bvidRegex.find(message)?.groupValues[0]?.let { bvid ->
            if (bvid.length == 12) {
                return bvid
            }
            null
        }

    }
    val shortRegex = link.toRegex()
    if (message.contains(shortRegex)) {
        // short link to long link

        val shortLink = shortRegex.find(message)!!.groupValues[1]
        val response = HTTPRequestUtil.call(shortLink)
        val bytes = response.body<ByteArray>()
        val decompressed = withContext(Dispatchers.IO) {
            GZIPInputStream(bytes.inputStream()).readBytes()
        }
        val text = decompressed.toString(Charsets.UTF_8)
        return bvidRegex.find(text)?.groupValues[0]
    }
    return null
}