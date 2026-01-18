package cn.luorenmu.action.request.api

import cn.luorenmu.action.request.HTTPRequestUtil
import com.alibaba.fastjson2.to
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 *
 * @author LoMu
 * Date 2025/11/1 02:29
 */

/**
 *  PakeApi 通常处理文本类数据，如：json、xml、text
 */
abstract class PakeApi(
    override var url: String,
    override var method: HttpMethod = HttpMethod.Get,
    override val headers: MutableMap<String, String> = mutableMapOf(),
    override val body: MutableMap<String, String> = mutableMapOf(),
) : Api {


    override fun toString(): String {
        return "Api(baseUrl='$baseUrl', url='$url', method=$method, headers=$headers, body=$body)"
    }

    protected suspend fun call(): HttpResponse {
        return HTTPRequestUtil.call(this)
    }

    protected suspend inline fun <reified T> callDTO(): T =
        HTTPRequestUtil.call(this).bodyAsText().to<T>()

}