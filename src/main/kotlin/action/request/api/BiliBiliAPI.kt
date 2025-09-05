package cn.luorenmu.action.request.api

import cn.luorenmu.entity.RequestEntity
import cn.luorenmu.file.ReadWriteFile

/**
 *
 * @author LoMu
 * Date 2025/8/24 08:58
 */
object BiliBiliAPI {
    val bilibiliHeaders = listOf(
        RequestEntity.RequestParam(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0"
        ),
        RequestEntity.RequestParam(
            "Referer", "https://www.bilibili.com/"
        ),
        RequestEntity.RequestParam(
            "Origin", "https://www.bilibili.com/"
        )
    )

    fun bvidToCid(bvid: String) = RequestEntity.RequestDetailed().apply {
        url = "https://api.bilibili.com/x/player/pagelist?bvid=${bvid}"
        method = "GET"
        headers = bilibiliHeaders
    }

    fun videoSteam(cid: Long, bvid: String) = RequestEntity.RequestDetailed().apply {
        url = "https://api.bilibili.com/x/player/wbi/playurl?cid=${cid}&qn=112&fnval=0&fnver=0&fourk=1&bvid=${bvid}"
        method = "GET"
        headers = bilibiliHeaders
    }

    fun info(bvid: String) = RequestEntity.RequestDetailed().apply {
        url = "https://api.bilibili.com/x/web-interface/view?bvid=${bvid}"
        method = "GET"
        headers = bilibiliHeaders
    }

    object Download {
        fun downloadVideo(url: String, outputPath: String): Boolean {
            val resp = HTTPRequest.requestRetry {
                it.url = url
                it.method = "GET"
                it.headers = bilibiliHeaders
            }

            resp?.let {
                val stream = resp.bodyStream()
                ReadWriteFile.writeStreamFile(outputPath, stream)
                return true
            }
            return false
        }
    }
}