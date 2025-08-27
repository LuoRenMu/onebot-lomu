package cn.luorenmu.action.commandProcess.bot

import cn.luorenmu.action.commandProcess.CommandProcess
import cn.luorenmu.action.commandProcess.bot.entity.BilibiliInfoFreeMarker
import cn.luorenmu.action.request.BilibiliRequestData
import cn.luorenmu.action.request.api.BiliBiliAPI
import cn.luorenmu.action.request.api.HTTPRequest
import cn.luorenmu.action.request.entity.bilibili.BilibiliVideoInfoStreamData
import cn.luorenmu.action.request.entiy.bilibili.BilibiliVideoInfoData
import cn.luorenmu.common.extensions.getFirstBot
import cn.luorenmu.common.extensions.sendMsg
import cn.luorenmu.common.utils.*
import cn.luorenmu.entiy.Request
import cn.luorenmu.listen.entity.BotRole
import cn.luorenmu.listen.entity.MessageSender
import cn.luorenmu.listen.entity.MessageType
import cn.luorenmu.repository.BilibiliVideoRepository
import cn.luorenmu.repository.entiy.BilibiliVideo
import cn.luorenmu.request.RequestController
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.toJSONString
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.BotContainer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull

/**
 *
 * @author LoMu
 * Date 2025/8/25 02:38
 */
@Component
class BiliBiliListenVideo(
    private val bilibiliRequestData: BilibiliRequestData,
    private val bilibiliVideoRepository: BilibiliVideoRepository,
    private val bilibiliEventListen: BilibiliEventListenCommand,
    private val redisUtils: RedisUtils,
    private val webPool: WebPool,
    private val botContainer: BotContainer,
    @Value("\${server.port}")
    private val port: String,
) : CommandProcess {
    private val bilibiliVideoLongLink = "BV1[0-9a-zA-Z]{9}"
    private val bilibiliVideoShortLink = "((https://bili2233.cn/([a-zA-Z0-9]+))|(https://b23.tv/([a-zA-Z0-9]+)))"
    private val prefixImagesUrl = "/local_images/bilibili/"
    private val log = KotlinLogging.logger { }

    override fun process(sender: MessageSender): String? {
        if (!bilibiliEventListen.state(sender.groupOrSenderId) && sender.messageType != MessageType.PRIVATE) {
            return null
        }

        val correctMsg = sender.message.replace("\\", "")
        findBilibiliLinkBvid(correctMsg)?.let { bvid ->
            // 视频信息
            val info = bilibiliRequestData.info(bvid) ?: run {
                log.info { "视频信息获取失败 message -> $sender.message" }
                return "视频信息获取失败"
            }

            val videoPath = PathUtils.getVideoPath("bilibili/$bvid.flv")
            val videoPathCQ = MsgUtils.builder().video(videoPath, "").build()
            // 视频限制时长 只有在首次监听到该视频 管理员时长发送才生效 否则不发送
            val limitTime = if (sender.role.roleNumber >= BotRole.ADMIN.roleNumber) 5 else 1
            bilibiliVideoRepository.findFirstBybvid(bvid)?.let { bilibili ->
                botContainer.getFirstBot().sendMsg(sender.messageType, sender.groupOrSenderId, bilibili.info)
                bilibili.videoPathCQ?.let { videoPathCQ ->
                    // 不为null 但是文件不存在 应当重新下载文件
                    if (File(bilibili.path!!).exists()) {
                        botContainer.getFirstBot().sendMsg(sender.messageType, sender.groupOrSenderId, videoPathCQ)
                        return null
                    }

                } ?: run {
                    //数据库中没有存储视频地址
                    return null
                }

            }


            // 下载视频并发送
            bilibiliRequestData.bvidToCid(bvid)?.let { videoStreamInfo ->
                val videoInfos = bilibiliRequestData.getVideoInfo(bvid, videoStreamInfo.cid)
                videoInfos?.let {
                    val minute = videoInfos.timelength / 1000 / 60
                    val suffixMessage = if (minute > limitTime) "视频过长 不发送" else "视频准备发送中"
                    val videoInfoStr =
                        MsgUtils.builder().reply(sender.messageId).img(getVideoInfoImage(info)).text(suffixMessage)
                            .build()
                    botContainer.getFirstBot().sendMsg(
                        sender.messageType, sender.groupOrSenderId,
                        videoInfoStr
                    )
                    if (minute > limitTime) {
                        return null
                    }
                    downloadVideo(videoInfos, videoPath)?.let { success ->
                        if (success) {
                            bilibiliVideoRepository.save(
                                BilibiliVideo(
                                    null,
                                    bvid,
                                    videoPath,
                                    videoPathCQ,
                                    videoInfoStr
                                )
                            )
                            botContainer.getFirstBot().sendMsg(sender.messageType, sender.groupOrSenderId, videoPathCQ)
                        } else {
                            bilibiliVideoRepository.save(
                                BilibiliVideo(
                                    null,
                                    bvid,
                                    null,
                                    null,
                                    videoInfoStr
                                )
                            )
                        }
                    } ?: run {
                        return "视频解析失败"
                    }
                }
            }
        }
        return null
    }


    private fun getVideoInfoImage(info: BilibiliVideoInfoData): String {
        val path = PathUtils.getImagePath("bilibili/video_info/${info.bvid}")
        webPool.getWebPageScreenshot().screenshotSelector(
            "http://localhost:$port/ftlh/${freeMarkerBuild(info)}",
            path,
            ".box"
        )
        return path
    }


    private fun freeMarkerBuild(info: BilibiliVideoInfoData): String {
        val localPic = "${info.bvid}-pic"
        val localAvatarName = UUID.randomUUID()
        HTTPRequest.downloadStream(info.pic, PathUtils.getImagePath("bilibili/${localPic}"))
        HTTPRequest.downloadStream(info.owner.face, PathUtils.getImagePath("bilibili/${localAvatarName}"))
        val content = FreeMarkerUtils.parseData(
            "bilibili_info.ftlh",
            JSON.parseObject(
                BilibiliInfoFreeMarker(
                    "$prefixImagesUrl$localPic",
                    "$prefixImagesUrl$localAvatarName",
                    info.owner.name,
                    info.title,
                    info.desc
                ).toJSONString()
            )
        )
        redisUtils.setCache("ftlh:${info.bvid}", content, 5, TimeUnit.MINUTES)
        return info.bvid
    }

    private fun findBilibiliLinkBvid(message: String): String? {
        if (message.contains(bilibiliVideoLongLink.toRegex())) {
            return MatcherData.matcherIndexStr(message, bilibiliVideoLongLink, 0).getOrNull()?.let { bvid ->
                if (bvid.length == 12) {
                    return bvid
                }
                null
            }

        }
        if (message.contains(bilibiliVideoShortLink.toRegex())) {
            // short link to long link
            val shortLink = MatcherData.matcherStr(message, bilibiliVideoShortLink, 1, "").getOrNull()

            val respBody = RequestController(Request.RequestDetailed().apply {
                url = shortLink!!
                method = "GET"
            }).request().body()

            return MatcherData.matcherIndexStr(respBody, bilibiliVideoLongLink, 0).getOrNull()
        }

        return null
    }


    /**
     *
     *  @param outputPath video save local path need file name and video type (default type is flv)
     *  @return null if video download failed  else false video too large (limit length $minute minute)
     */
    private fun downloadVideo(
        videoInfos: BilibiliVideoInfoStreamData,
        outputPath: String,
    ): Boolean? {
        videoInfos.let {
            videoInfos.durl.firstOrNull()?.let { videoInfo ->
                if (BiliBiliAPI.Download.downloadVideo(videoInfo.url, outputPath)) {
                    return true
                }
                return null
            }
        }
        return false
    }

    override fun commandName(): String =
        "BiliBiliListenVideo"


    override fun state(id: Long): Boolean =
        true


    override fun command(): Regex =
        Regex("((https://bili2233.cn/([a-zA-Z0-9]+))|(https://b23.tv/([a-zA-Z0-9]+))|(BV1[0-9a-zA-Z]{9}))")


    override fun needAtBot(): Boolean = false
}