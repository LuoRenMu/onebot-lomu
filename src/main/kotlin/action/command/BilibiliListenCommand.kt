package cn.luorenmu.action.command

import cn.luorenmu.action.commandProcess.CommandProcess
import cn.luorenmu.action.commandProcess.bot.entity.BilibiliInfoFreeMarker
import cn.luorenmu.action.request.HTTPRequestUtil
import cn.luorenmu.action.request.api.BiliBiliAPI
import cn.luorenmu.action.request.entity.BilibiliVideoInfoData
import cn.luorenmu.action.request.entity.BilibiliVideoInfoStreamData
import cn.luorenmu.common.extensions.getFirstBot
import cn.luorenmu.common.extensions.sendMsg
import cn.luorenmu.common.utils.FreeMarkerUtils
import cn.luorenmu.common.utils.PathUtils
import cn.luorenmu.common.utils.ReadWriteFile
import cn.luorenmu.exception.LoMuBotException
import cn.luorenmu.listen.entity.MessageSender
import cn.luorenmu.service.WebPool
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.BotContainer
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.GZIPInputStream

/**
 *
 * @author LoMu
 * Date 2025/11/7 22:35
 */
@Component
class BilibiliListenCommand(
    private val botContainer: BotContainer,
    private val webPool: WebPool,
) : CommandProcess {
    private val bilibiliBVID = "BV1[0-9a-zA-Z]{9}"

    private val bilibiliVideoShortLink = """(https:\\?/\\?/(?:bili2233\.cn|b23\.tv)\\?/([a-zA-Z0-9]+))"""
    private val log = KotlinLogging.logger { }

    override suspend fun process(sender: MessageSender): String? {
        val correctMsg = sender.message.replace("\\", "")
        findBilibiliLinkBvid(correctMsg)?.let { bvid ->
            // 视频信息
            val info = BiliBiliAPI.Info(bvid).execute().data.first()
            val videoPath = PathUtils.getVideoPath("bilibili/$bvid.flv")
            val videoPathCQ = MsgUtils.builder().video(videoPath, "").build()
            val limitTime = 15
            // 下载视频并发送
            BiliBiliAPI.BvidToCid(bvid).execute().let { videoStreamInfo ->
                val videoInfos =
                    BiliBiliAPI.VideoSteam(videoStreamInfo.data.first().cid, bvid).execute().data.first()
                videoInfos.let {
                    val minute = videoInfos.timelength / 1000 / 60
                    supervisorScope {
                        launch {
                            if (minute <= limitTime) {
                                if (!File(videoPath).exists()) {
                                    try {
                                        downloadVideo(videoInfos, videoPath)
                                    } catch (e: Exception) {
                                        log.error(e) { "视频下载失败" }
                                        botContainer.getFirstBot().sendMsg(
                                            sender.messageType, sender.groupOrSenderId,
                                            "视频下载失败"
                                        )
                                    }
                                }
                                botContainer.getFirstBot().sendMsg(
                                    sender.messageType, sender.groupOrSenderId,
                                    videoPathCQ
                                )
                            }
                        }
                        launch {
                            val suffixMessage = if (minute > limitTime) "视频超过${limitTime}分钟 不发送" else ""
                            val videoInfoStr =
                                MsgUtils.builder().reply(sender.messageId).img(getVideoInfoImage(info))
                                    .text(suffixMessage)
                                    .build()
                            botContainer.getFirstBot().sendMsg(
                                sender.messageType, sender.groupOrSenderId,
                                videoInfoStr
                            )
                        }
                    }
                }
            }
        }
        return null
    }


    private suspend fun getVideoInfoImage(info: BilibiliVideoInfoData): String {
        val pic = PathUtils.getImagePath("bilibili/${info.bvid}-pic.png")
        val bodyAsBytes = HTTPRequestUtil.call(
            info.pic,
        ).bodyAsBytes()
        ReadWriteFile.writeStreamFile(
            pic, ByteArrayInputStream(bodyAsBytes)
        )
        val bodyAsBytes1 = HTTPRequestUtil.call(info.owner.face).bodyAsBytes()

        val avatar = PathUtils.getImagePath(
            "bilibili/${info.owner.mid}.png"
        )
        ReadWriteFile.writeStreamFile(
            avatar, ByteArrayInputStream(bodyAsBytes1)
        )

        val parse = FreeMarkerUtils.parseData(
            "bilibili_info.ftlh",
            BilibiliInfoFreeMarker(
                pic,
                avatar,
                info.owner.name,
                info.title,
                if (info.desc.length > 100) info.desc.substring(0, 100) + "..." else info.desc,
            )
        )
        val page = PathUtils.getRenderPath("bilibili/${info.bvid}.html")
        ReadWriteFile.writeStreamFile(
            page, parse.toByteArray().inputStream()
        )

        val render = PathUtils.getImagePath("bilibili/render/${info.bvid}.png")
        webPool.getWebPageScreenshot().screenshotSelector(
            page,
            render,
            "#box"
        )
        return render
    }

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


    private suspend fun downloadVideo(
        videoInfos: BilibiliVideoInfoStreamData,
        outputPath: String,
    ) {
        try {
            videoInfos.let {
                videoInfos.durl.firstOrNull()?.let { videoInfo ->
                    BiliBiliAPI.Download.Video(videoInfo.url, outputPath).execute()
                }
            }
        } catch (e: Exception) {
            log.error { "视频下载失败 $e" }
            throw LoMuBotException("视频下载失败")
        }
    }

    override fun commandName(): String =
        "BiliBiliListenVideo"


    override fun state(id: Long): Boolean =
        true


    override fun command(): Regex =
        Regex("(${bilibiliVideoShortLink}|${bilibiliBVID})")

    override fun needAtBot(): Boolean = false
}