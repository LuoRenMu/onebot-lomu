package cn.luorenmu.common.utils

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author LoMu
 * Date 2025.05.29 12:04
 */
class WebPool(size: Int, headless: Boolean = true) {
    private val webPageScreenshots = run {
        val item = CopyOnWriteArrayList<WebPageScreenshot>()
        (1..size).forEach { i ->
            item.add(WebPageScreenshot(headless))
        }
        item
    }
    private val index = AtomicInteger(0)

    fun getWebPageScreenshot(): WebPageScreenshot {
        val idx =  index.getAndUpdate { (it + 1) %  webPageScreenshots.size }
        return webPageScreenshots[idx]
    }

    fun shutdown() {
        webPageScreenshots.forEach {
            it.shutdown()
        }
    }
}