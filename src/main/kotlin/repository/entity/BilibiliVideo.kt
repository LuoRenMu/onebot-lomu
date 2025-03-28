package cn.luorenmu.repository.entiy

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 * @author LoMu
 * Date 2024.10.07 20:16
 */
@Document(collection = "bilibili_video")
data class BilibiliVideo(
    var id: String?,
    @Id
    @Indexed
    val bvid: String,
    val path: String?,
    val videoPathCQ: String?,
    val info: String,

    )
