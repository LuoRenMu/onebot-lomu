package cn.luorenmu.action.request.api

import cn.luorenmu.action.request.entity.BilibiliPageListInfo
import cn.luorenmu.action.request.entity.BilibiliVideoInfoResponse
import cn.luorenmu.action.request.entity.BilibiliVideoStreamInfo
import cn.luorenmu.common.utils.ReadWriteFile
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 *
 * @author LoMu
 * Date 2025/8/24 08:58
 */
sealed class BiliBiliAPI<T>(
    override var url: String,
    override var method: HttpMethod = HttpMethod.Get,
    override val headers: MutableMap<String, String> = mutableMapOf(),
    override val body: MutableMap<String, String> = mutableMapOf(),
) : PakeApi(
    url,
    method,
    headers,
    body
) {
    override var baseUrl: String = ""

    abstract suspend fun execute(): T

    init {
        headers.putAll(
            mapOf(
                "User-Agent" to
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36",
                "Referer" to
                        "https://www.bilibili.com/",
                "Origin" to
                        "https://www.bilibili.com/",
            )
        )
    }

    class BvidToCid(bvid: String) :
        BiliBiliAPI<BilibiliPageListInfo>("https://api.bilibili.com/x/player/pagelist?bvid=${bvid}") {
        override suspend fun execute(): BilibiliPageListInfo {
            return callDTO<BilibiliPageListInfo>()
        }
    }

    class VideoSteam(cid: Long, bvid: String) :
        BiliBiliAPI<BilibiliVideoStreamInfo>("https://api.bilibili.com/x/player/wbi/playurl?cid=${cid}&qn=112&fnval=0&fnver=0&fourk=1&bvid=${bvid}") {
        override suspend fun execute(): BilibiliVideoStreamInfo {
            return callDTO()
        }
    }

    class Info(bvid: String) :
        BiliBiliAPI<BilibiliVideoInfoResponse>("https://api.bilibili.com/x/web-interface/view?bvid=${bvid}") {
        override suspend fun execute(): BilibiliVideoInfoResponse {
            return callDTO<BilibiliVideoInfoResponse>()
        }
    }

    sealed class Download(url: String) : BiliBiliAPI<Unit>(url) {
        class Video(url: String, val outputPath: String) : Download(url) {
            override suspend fun execute() {
                val inputStream = call().bodyAsBytes().inputStream()
                ReadWriteFile.writeStreamFile(outputPath, inputStream)
            }
        }
    }
}