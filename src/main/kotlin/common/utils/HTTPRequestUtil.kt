package cn.luorenmu.common.utils

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

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

    val json = Json {
        ignoreUnknownKeys = true
    }

    val client = HttpClient(CIO) {
        install(HttpRequestRetry) {
            maxRetries = 3
            retryOnServerErrors(maxRetries = 3)
            retryIf { request, response ->
                !response.status.isSuccess() && response.status.value != 404
            }
            exponentialDelay()
            delayMillis { retry ->
                retry * 1000L
            }
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
        json.decodeFromString<T>(call(RequestEntity(url)).bodyAsText())

    suspend inline fun <reified T> callDTO(requestEntity: RequestEntity): T =
        json.decodeFromString<T>(call(requestEntity).bodyAsText())

    suspend fun call(requestEntity: RequestEntity): HttpResponse {
        log.info { "http request ${requestEntity.method} -> ${requestEntity.url} " }
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

    }

    inline fun <reified T> requestCacheJson(key: String, requestEntity: RequestEntity): T {
        var t: T? = null
        val tJson = jsonCache.get(key) {
            t = runBlocking { json.decodeFromString<T>(call(requestEntity).bodyAsText()) }
            json.encodeToString(t)
        }
        return t ?: json.decodeFromString<T>(tJson)
    }

    @Serializable
    class RequestEntity(val url: String, @Contextual val method: HttpMethod = HttpMethod.Get) {
        var params: MutableList<RequestParam>? = null
        var body: MutableList<RequestParam>? = null
        var bodyJson: String? = null
        var headers: MutableList<RequestParam>? = null

        @Serializable
        class RequestParam(val name: String, val content: String) {
            constructor(p: Pair<String, String>) : this(p.first, p.second)
        }
    }

}