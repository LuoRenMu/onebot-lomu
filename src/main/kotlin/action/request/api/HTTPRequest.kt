package cn.luorenmu.action.request.api

import cn.hutool.http.HttpResponse
import cn.luorenmu.file.ReadWriteFile
import cn.luorenmu.request.RequestController
import cn.luorenmu.request.entity.RequestEntity.RequestDetailed
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 *
 * @author LoMu
 * Date 2025/8/24 09:08
 */
object HTTPRequest {
    private val log = KotlinLogging.logger {}

    fun downloadStream(url: String, path: String) {
        val requestController = RequestController(RequestDetailed().apply {
            this.url = url
            method = "GET"
        })
        val resp = requestController.request()
        ReadWriteFile.writeStreamFile(path, resp.bodyStream())
    }

    fun requestRetry(requestController: RequestController, retry: Int = 5): HttpResponse? {
        if (retry == 0) {
            return null
        }

        try {
            val resp = requestController.request()
            return when (resp?.status) {
                200 -> resp
                404 -> null
                else -> requestRetry(requestController, retry - 1)
            }
        } catch (e: Exception) {
            log.error { e }
            return requestRetry(requestController, retry - 1)
        }
    }

    fun requestRetry(requestDetailedLmd: (RequestDetailed) -> Unit, retry: Int = 3): HttpResponse? {
        val requestDetailed = RequestDetailed()
        requestDetailedLmd(requestDetailed)
        val requestController = RequestController(requestDetailed)
        return requestRetry(requestController, retry)
    }


    fun requestRetry(requestDetailedLmd: (RequestDetailed) -> Unit): HttpResponse? {
        return requestRetry(requestDetailedLmd, 3)
    }

    fun requestRetry(requestDetailedLmd: RequestDetailed): HttpResponse? {
        val requestController = RequestController(requestDetailedLmd)
        return requestRetry(requestController, 3)
    }
}