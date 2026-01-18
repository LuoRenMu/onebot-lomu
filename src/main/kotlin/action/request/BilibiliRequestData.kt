package cn.luorenmu.action.request

import cn.luorenmu.action.request.api.BiliBiliAPI
import cn.luorenmu.action.request.entity.BilibiliPageListInfo
import cn.luorenmu.action.request.entity.BilibiliVideoInfoResponse
import cn.luorenmu.action.request.entity.BilibiliVideoStreamInfo
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

    suspend fun getVideoInfo(bvid: String, cid: Long): BilibiliVideoStreamInfo {
        return BiliBiliAPI.videoSteam(cid, bvid).callDTO<BilibiliVideoStreamInfo>()
    }

    suspend fun info(bvid: String): BilibiliVideoInfoResponse =
        BiliBiliAPI.info(bvid).callDTO<BilibiliVideoInfoResponse>()


    suspend fun bvidToCid(bvid: String): BilibiliPageListInfo =
        BiliBiliAPI.bvidToCid(bvid).callDTO<BilibiliPageListInfo>()

}