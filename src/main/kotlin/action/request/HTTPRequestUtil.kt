package cn.luorenmu.action.request

import cn.luorenmu.action.request.api.Api
import cn.luorenmu.action.request.api.PakeApi
import cn.luorenmu.common.utils.ReadWriteFile
import cn.luorenmu.exception.LoMuBotException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.cache.storage.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.network.sockets.SocketTimeoutException
import java.nio.file.Files
import kotlin.io.path.Path

/**
 * @author LoMu
 * Date 2023.11.21 21:12
 */
object HTTPRequestUtil {

    val log = KotlinLogging.logger {}

    val client = HttpClient(CIO) {
        install(HttpRequestRetry) {
            maxRetries = 5
            retryOnServerErrors(maxRetries = 3)
            retryOnExceptionIf { _, cause ->
                when (cause) {
                    is SocketTimeoutException,
                    is ConnectTimeoutException,
                    is ClientRequestException,
                    is HttpRequestTimeoutException,
                        -> true

                    else -> false
                }
            }


        }
        install(HttpCache.Companion) {
            val cacheFile = Files.createDirectories(Path(ReadWriteFile.currentPathFileName("/cache"))).toFile()
            publicStorage(FileStorage(cacheFile))
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
        }

        defaultRequest {
            header(
                HttpHeaders.UserAgent,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 Edg/126.0.0.0"
            )
        }
    }

    suspend fun call(url: String): HttpResponse = call(object : PakeApi(url) {
        override var baseUrl: String = ""
    })

    suspend fun call(requestEntity: Api, timeoutMillis: Long = 0): HttpResponse {
        log.info { "http request ${requestEntity.method} -> ${requestEntity.url} " }
        try {
            return client.request {
                url(requestEntity.url)
                method = requestEntity.method
                requestEntity.body.let { by ->
                    setBody(by)
                    header(HttpHeaders.ContentType, "application/json")
                }
                requestEntity.headers.let { reqHeaders ->
                    reqHeaders.forEach { h ->
                        header(h.key, h.value)
                    }
                }
                if (timeoutMillis != 0L) {
                    timeout {
                        requestTimeoutMillis = timeoutMillis
                    }
                }
            }
        } catch (e: Exception) {
            log.error(e) { "Error during request ${e.printStackTrace()}" }
            throw LoMuBotException("请求期间出错, 无法连接到目标或被目标主机拒绝连接")
        }

    }

}