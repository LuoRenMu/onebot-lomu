package cn.luorenmu.action.request

import cn.luorenmu.common.utils.HTTPRequestUtil
import cn.luorenmu.common.utils.ReadWriteFile
import cn.luorenmu.exception.LoMuBotException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.statement.*
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.File


/**
 * @author LoMu
 * Date 2025.01.07 04:27
 */
@Component
class QQRequestData {

    private val log = KotlinLogging.logger { }

    init {
        ReadWriteFile.createCurrentDirs("image/qq/avatar")
    }

    /**
     * 下载qq头像
     */
    suspend fun downloadQQAvatar(qq: String): String {
        val avatarPath = ReadWriteFile.currentPathFileName("image/qq/avatar/${qq}.png")
        // 保证图片为最新
        synchronized(QQRequestData::class.java) {
            try {
                val file = File(avatarPath)
                if (file.exists()) {
                    file.delete()
                }
                // 如果抛出了错误 大概率是图片正在使用 不能删除
            } catch (_: Exception) {
                return avatarPath
            }
        }

        val requestUrl = getAvatarUrlString(qq, 640)
        try {
            val resp = HTTPRequestUtil.call(requestUrl)
            // 通过响应头判断是否存在当前分辨率图片
            if (resp.headers["Cache-Control"] == "no-cache") {
                val resp = HTTPRequestUtil.call(requestUrl.substring(0, requestUrl.length - 3) + "100")
                ReadWriteFile.writeStreamFile(avatarPath, ByteArrayInputStream(resp.bodyAsBytes()))
                return avatarPath
            }
            ReadWriteFile.writeStreamFile(avatarPath, ByteArrayInputStream(resp.bodyAsBytes()))
        } catch (e: Exception) {
            log.error { "获取qq头像失败 ${e.printStackTrace()}" }
            throw LoMuBotException("获取qq头像失败o(╥﹏╥)o")
        }
        return avatarPath
    }

    fun getAvatarUrlString(qq: String, size: Int = 640): String {
        return "https://q.qlogo.cn/headimg_dl?dst_uin=${qq}&spec=${size}"
    }
}