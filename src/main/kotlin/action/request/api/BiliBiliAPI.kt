package cn.luorenmu.action.request.api

import cn.luorenmu.entiy.Request
import cn.luorenmu.file.ReadWriteFile

/**
 *
 * @author LoMu
 * Date 2025/8/24 08:58
 */
object BiliBiliAPI {
    val bilibiliHeaders = listOf(
        Request.RequestParam().apply {
            name = "User-Agent"
            content =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0"
        },
        Request.RequestParam().apply {
            name = "Referer"
            content = "https://www.bilibili.com/"
        },
        Request.RequestParam().apply {
            name = "Origin"
            content = "https://www.bilibili.com/"
        }
    )

    fun bvidToCid(bvid: String) = Request.RequestDetailed().apply {
        url = "https://api.bilibili.com/x/player/pagelist?bvid=${bvid}"
        method = "GET"
        headers = bilibiliHeaders
    }

    fun videoSteam(cid: Long, bvid: String) = Request.RequestDetailed().apply {
        url = "https://api.bilibili.com/x/player/wbi/playurl?cid=${cid}&qn=112&fnval=0&fnver=0&fourk=1&bvid=${bvid}"
        method = "GET"
        headers = bilibiliHeaders
    }

    fun info(bvid: String) = Request.RequestDetailed().apply {
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