package cn.luorenmu.common.utils

import cn.luorenmu.exception.LoMuBotException
import com.alibaba.fastjson2.to
import com.alibaba.fastjson2.toJSONString
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path

/**
 * @author LoMu
 * Date 2023.11.21 21:12
 */
object HTTPRequestUtil {

    val log = KotlinLogging.logger {}
    val jsonCache: Cache<String, String> = Caffeine.newBuilder()
        .maximumSize(300)
        .expireAfterWrite(4, TimeUnit.HOURS)
        .build()


    val client = HttpClient(CIO) {
        install(HttpRequestRetry) {
            maxRetries = 5
            retryOnServerErrors(maxRetries = 3)

            retryOnExceptionIf { request, cause ->
                when (cause) {
                    is SocketTimeoutException,
                    is ConnectTimeoutException,
                    is ClientRequestException,
                    is HttpRequestTimeoutException,
                        -> true

                    else -> false
                }
            }
            exponentialDelay()
            delayMillis { retry ->
                retry * 1000L
            }

        }
        install(HttpCache) {
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


    suspend fun call(url: String): HttpResponse = call(RequestEntity(url))

    // 返回对象
    suspend inline fun <reified T> callDTO(url: String): T =
        call(RequestEntity(url)).bodyAsText().to<T>()

    suspend inline fun <reified T> callDTO(requestEntity: RequestEntity): T =
        call(requestEntity).bodyAsText().to<T>()

    suspend fun call(requestEntity: RequestEntity): HttpResponse {
        log.info { "http request ${requestEntity.method} -> ${requestEntity.url} " }
        try {
            return client.request {
                url(requestEntity.url)
                method = requestEntity.method
                requestEntity.body?.let { by ->
                    setBody(by)
                    header(HttpHeaders.ContentType, "application/json")
                }
                requestEntity.headers?.let { reqHeaders ->
                    reqHeaders.forEach { h ->
                        header(h.name, h.content)
                    }
                }
            }
        } catch (e: Exception) {
            log.error(e) { "Error during request ${e.printStackTrace()}" }
            throw LoMuBotException("请求期间出错, 无法连接到目标或被目标主机拒绝连接")
        }

    }

    inline fun <reified T> requestCacheJson(key: String, requestEntity: RequestEntity): T {
        var t: T? = null
        val tJson = jsonCache.get(key) {
            t = runBlocking { call(requestEntity).bodyAsText().to<T>() }
            t.toJSONString()
        }
        return t ?: tJson.to<T>()
    }

    @Serializable
    data class RequestEntity(val url: String, @Contextual val method: HttpMethod = HttpMethod.Get) {
        var body: MutableList<RequestParam>? = null
        var headers: MutableList<RequestParam>? = null

        @Serializable
        data class RequestParam(val name: String, val content: String) {
            infix fun String.to(that: String): RequestParam = RequestParam(this, that)
        }
    }

}