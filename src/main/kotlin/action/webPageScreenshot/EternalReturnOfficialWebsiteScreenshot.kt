package cn.luorenmu.action.webPageScreenshot

import cn.luorenmu.action.request.api.EternalReturnOfficialAPI
import cn.luorenmu.service.WebPool
import org.springframework.stereotype.Component

/**
 * @author LoMu
 * Date 2025.02.20 13:55
 */
@Component
class EternalReturnOfficialWebsiteScreenshot(
    private val webPool: WebPool,
) {

    /**
     * 单任务线程 底层已经保证了其线程安全 避免重复截取
     * @param id 公告id
     */
    @Synchronized
    fun screenshotNews(id: String, outputPath: String, failed: Int = 0): String {
        val url = EternalReturnOfficialAPI.WEBSITE_URL + id
        try {
            webPool.getWebPageScreenshot().screenshotSelector(
                url,
                outputPath,
                ".er-article-detail__content.er-article-content.fr-view"
            ) {
                it.evaluate("document.querySelector('#gnb').remove()")
            }
        } catch (_: Exception) {
            if (failed < 5) {
                return screenshotNews(id, outputPath, failed + 1)
            }
            throw RuntimeException("EternalReturnOfficialWebsiteScreenshot 重试次数过多")
        }
        return outputPath
    }
}