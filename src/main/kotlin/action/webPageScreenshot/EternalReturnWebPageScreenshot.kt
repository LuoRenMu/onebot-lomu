package cn.luorenmu.action.webPageScreenshot

import cn.luorenmu.action.request.api.EternalReturnDakGGAPI
import cn.luorenmu.common.extensions.toPinYin
import cn.luorenmu.common.utils.HTTPRequestUtil
import cn.luorenmu.common.utils.PathUtils
import cn.luorenmu.common.utils.WebPool
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitUntilState
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.OneBotMedia
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * @author LoMu
 * Date 2024.08.03 9:17
 */
@Component
class EternalReturnWebPageScreenshot(
    private val webPool: WebPool,
) {
    private val cache: Cache<String, String> = Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(12, TimeUnit.HOURS)
        .build()
    private val log = KotlinLogging.logger { }


    // 角色页面
    fun webCharacterScreenshot(inputName: String, character: String, weapon: String, failed: Int = 0): String {
        val cacheName = "Eternal_Return: nickname :${character}_${inputName.toPinYin()}_${weapon}"
        cache.getIfPresent(cacheName)?.let {
            log.info { "命中缓存: $character" }
            return cacheName
        }

        val path =
            PathUtils.getEternalReturnImagePath("nickname/${character}-${inputName.toPinYin()}-${weapon}.png")
        log.info { "正在进行截图: $character" }
        val url = EternalReturnDakGGAPI.PageURL.characterPageURL(character, weaponType = weapon)
        try {
            webPool.getWebPageScreenshot().screenshotSelector(url, path, ".contents") {
                TimeUnit.SECONDS.sleep(3)
                it.locator("div.title h3").evaluate(
                    """node => {
                                    node.innerHTML = node.innerHTML.replace(
                                        /(<strong>.*?<\/strong>)([^<]+)/, 
                                        "$1${inputName}"
                                    );
                              }"""
                )
            }
        } catch (_: Exception) {
            if (failed < 3) {
                log.error { "页面截图:失败$failed 次 再次重试 $character" }
                return webCharacterScreenshot(inputName, character, weapon, failed + 1)
            }
            return "重试次数过多 网络无法连接"
        }
        val returnMsg = MsgUtils.builder().img(path).build()
        log.info { "已完成的截图: $character" }
        cache.put(cacheName, returnMsg)
        return returnMsg
    }

    /**
     * 玩家战绩页面截图
     */
    fun webPlayerPageScreenshot(nickname: String): String {
        val url = EternalReturnDakGGAPI.PageURL.playerPageURL(nickname)
        log.info { "正在进行截图: $nickname" }
        val path = PathUtils.getEternalReturnNicknameImagePath(nickname)
        val returnMsg = MsgUtils.builder().img(OneBotMedia().file(path).cache(false).proxy(false)).build()

        webPool.getWebPageScreenshot().screenshotSelector(
            url,
            path, "#content-container"
        ) { TimeUnit.SECONDS.sleep(3) }

        log.info { "已完成的截图: $nickname" }
        return returnMsg
    }

    fun webRoutesPageScreenshot(routesId: String, failed: Int = 0): String {
        val imgPath = PathUtils.getEternalReturnImagePath("routes/$routesId.png")
        val requestUrl = EternalReturnDakGGAPI.PageURL.ROUTES_URL + routesId

        if (failed == 0) {
            runBlocking {
                val resp = HTTPRequestUtil.call(requestUrl)
                return@runBlocking if (resp.status.value == 307) {
                    "未找到该路线"
                } else null
            }?.let { return it }
        }

        try {
            webPool.getWebPageScreenshot().screenshotSelector(requestUrl, imgPath, "#content-container") {
                TimeUnit.SECONDS.sleep(3)
            }
        } catch (_: Exception) {
            if (failed < 3) {
                log.error { "页面截图:失败$failed 次 再次重试 $routesId" }
                return webRoutesPageScreenshot(routesId, failed + 1)
            }
            return "重试次数过多 网络无法连接"
        }

        return MsgUtils.builder().img(imgPath).build()
    }


    fun webCharacterStatisticsPageScreenshot(failed: Int = 0): String {
        val imgPath = PathUtils.getEternalReturnImagePath("character_statistics.png")
        val returnMsg = MsgUtils.builder().img(imgPath).build()
        val cacheName = "Eternal_Return: character_statistics"
        try {
            return cache.get(cacheName) {
                webPool.getWebPageScreenshot()
                    .customizeSelector(
                        EternalReturnDakGGAPI.PageURL.CHARACTER_STATISTICS_URL,
                        "#content-container",
                        WaitUntilState.DOMCONTENTLOADED
                    ) { page, box ->
                        TimeUnit.SECONDS.sleep(5)
                        val scrollHeight = page.evaluate("document.body.scrollHeight").toString().toDouble()
                        page.screenshot(
                            Page.ScreenshotOptions().setPath(Paths.get(imgPath))
                                .setFullPage(true)
                                .setClip(box.x, box.y, box.width, scrollHeight - 800)
                        )
                    }
                returnMsg
            } ?: returnMsg
        } catch (_: Exception) {
            if (failed < 3) {
                log.error { "页面截图:失败$failed 次 再次重试 Statistics" }
                return webCharacterStatisticsPageScreenshot(failed + 1)
            }
            return "重试次数过多 网络无法连接"
        }
    }
}