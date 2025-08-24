package cn.luorenmu.action.request

import cn.luorenmu.action.request.api.BiliBiliAPI
import cn.luorenmu.action.request.api.HTTPRequest
import cn.luorenmu.action.request.entity.bilibili.BilibiliPageInfoData
import cn.luorenmu.action.request.entity.bilibili.BilibiliPageListInfo
import cn.luorenmu.action.request.entity.bilibili.BilibiliVideoInfoStreamData
import cn.luorenmu.action.request.entity.bilibili.BilibiliVideoStreamInfo
import cn.luorenmu.action.request.entiy.bilibili.BilibiliVideoInfoData
import cn.luorenmu.action.request.entiy.bilibili.BilibiliVideoInfoResponse
import com.alibaba.fastjson2.to
import org.springframework.stereotype.Component

/**
 * @author LoMu
 * Date 2024.09.12 21:19
 */

/**
 * 0：成功
 * -400：请求错误
 * -404：无视频
 */
@Component
class BilibiliRequestData {


    fun getVideoInfo(bvid: String, cid: Long): BilibiliVideoInfoStreamData? {
        val resp = HTTPRequest.requestRetry(BiliBiliAPI.videoSteam(cid, bvid))
        resp?.let {
            val body = it.body()
            val result = body.to<BilibiliVideoStreamInfo>()
            if (result.code == 0) {
                return result.data.firstOrNull()
            }
        }
        return null
    }

    fun info(bvid: String): BilibiliVideoInfoData? {

        val resp = HTTPRequest.requestRetry(BiliBiliAPI.info(bvid))
        resp?.let {
            try {
                val result = it.body().to<BilibiliVideoInfoResponse>()
                return result.data.firstOrNull()
            } catch (_: Exception) {
                return null
            }

        }
        return null
    }


    fun bvidToCid(bvid: String): BilibiliPageInfoData? {
        val resp = HTTPRequest.requestRetry(BiliBiliAPI.bvidToCid(bvid))
        resp?.let {
            val result = it.body().to<BilibiliPageListInfo>()
            return result.data.firstOrNull()
        }
        return null
    }
}