package cn.luorenmu.action.request.entity

import com.alibaba.fastjson2.annotation.JSONField

/**
 *
 * @author LoMu
 * Date 2025/11/7 22:47
 */
data class BilibiliPageListInfo(
    var code: Int,
    var message: String,
    var ttl: Int,
    var data: List<BilibiliPageInfoData>,
)

data class BilibiliPageInfoData(
    var cid: Long,
    var page: Int,
    var from: String,
    var part: String,
    var duration: Int,
    var vid: String,
    var weblink: String,
    @JSONField(name = "first_frame") val firstFrame: String?,

    )