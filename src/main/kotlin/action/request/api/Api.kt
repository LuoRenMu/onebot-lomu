package cn.luorenmu.action.request.api

import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 *
 * @author LoMu
 * Date 2025/10/25 17:27
 */
sealed interface Api {
    var baseUrl: String
    var url: String
    var method: HttpMethod
    val headers: MutableMap<String, String>
    val body: MutableMap<String, String>

    companion object {
        fun <T> CoroutineScope.ioAsync(block: suspend CoroutineScope.() -> T) =
            async(Dispatchers.IO, block = block)

        fun CoroutineScope.ioLaunch(block: suspend CoroutineScope.() -> Unit) =
            launch(Dispatchers.IO, block = block)
    }
}