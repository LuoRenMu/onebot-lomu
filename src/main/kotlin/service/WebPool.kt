package cn.luorenmu.service

import cn.luorenmu.common.utils.WebPageScreenshot
import cn.luorenmu.config.external.LoMuProperties
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author LoMu
 * Date 2025.05.29 12:04
 */
@Service
class WebPool(
    private val loMuProperties: LoMuProperties,
) {

    private val webPageScreenshots = run {
        val item = CopyOnWriteArrayList<WebPageScreenshot>()
        (1..loMuProperties.web.size).forEach { i ->
            item.add(WebPageScreenshot(loMuProperties.web.headless))
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