package cn.luorenmu.service

import cn.luorenmu.common.utils.WebPageScreenshot
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author LoMu
 * Date 2025.05.29 12:04
 */
@Service
class WebPool(
    @Value("\${web.pool:3}")
    private val size: Int,
    @Value("\${web.headless:true}")
    private val headless: Boolean,
) {

    private val webPageScreenshots = run {
        val item = CopyOnWriteArrayList<WebPageScreenshot>()
        (1..size).forEach { i ->
            item.add(WebPageScreenshot(headless))
        }
        item
    }
    private val index = AtomicInteger(0)

    fun getWebPageScreenshot(): WebPageScreenshot {
        val idx = index.getAndUpdate { (it + 1) % webPageScreenshots.size }
        return webPageScreenshots[idx]
    }

    @PreDestroy
    fun shutdown() {
        webPageScreenshots.forEach {
            it.shutdown()
        }
    }
}