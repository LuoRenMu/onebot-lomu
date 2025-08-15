package cn.luorenmu.action.request

import cn.luorenmu.common.utils.RedisUtils
import cn.luorenmu.entiy.Request.RequestDetailed
import cn.luorenmu.exception.LoMuBotException
import cn.luorenmu.file.ReadWriteFile
import cn.luorenmu.request.RequestController
import org.springframework.stereotype.Component
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * @author LoMu
 * Date 2025.01.07 04:27
 */
@Component
class QQRequestData(
    private val redisUtils: RedisUtils,
) {
    /**
     * 下载qq头像
     */
    fun downloadQQAvatar(qq: String): String {

        val avatarPath = ReadWriteFile.currentPathFileName("image/qq/avatar/${qq}.png")
        redisUtils.getCache("qqAvatar:$qq", String::class.java, null, 1L, TimeUnit.DAYS)?.let {
            return it
        }

        // 保证图片为最新
        synchronized(QQRequestData::class.java) {
            try {
                val file = File(avatarPath)
                if (file.exists()) {
                    file.delete()
                }
                // 如果抛出了错误 大概率是图片正在使用 不能删除
            } catch (_: Exception){
                return avatarPath
            }
        }

        val requestUrl = "https://q1.qlogo.cn/g?b=qq&nk=$qq&s=640"
        val requestDetailed = RequestDetailed().apply {
            url = requestUrl
            method = "GET"
        }
        try {
            val requestController = RequestController(requestDetailed)
            val resp = requestController.request()
            // 通过响应头判断是否存在当前分辨率图片
            if (resp.header("Cache-Control") == "no-cache") {
                requestDetailed.url = requestUrl.substring(0, requestUrl.length - 3) + "100"
                val requestController = RequestController(requestDetailed)
                val resp = requestController.request()
                ReadWriteFile.writeStreamFile(avatarPath, resp.bodyStream())
                return avatarPath
            }
            ReadWriteFile.writeStreamFile(avatarPath, resp.bodyStream())
        } catch (e: Exception) {
            throw LoMuBotException("获取qq头像失败->${requestUrl},${e}")
        }
        redisUtils.setCacheIfAbsent("qqAvatar:$qq", avatarPath)
        return avatarPath
    }
}