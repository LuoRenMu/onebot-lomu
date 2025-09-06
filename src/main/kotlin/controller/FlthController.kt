package cn.luorenmu.controller

import cn.luorenmu.common.utils.CaffeineUtils
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody

/**
 * @author LoMu
 * Date 2025.03.18 21:03
 */
@Controller
class FlthController(
    private val caffeineUtils: CaffeineUtils,
) {

    @GetMapping("/ftlh/{id}")
    @ResponseBody
    fun getFtlh(@PathVariable id: String, httpResponse: HttpServletResponse): String {
        val cacheKey = "ftlh:$id"
        val cache = caffeineUtils.getCache(cacheKey, String::class.java)
            ?: run {
                httpResponse.status = 404
                return "404"
            }
        caffeineUtils.deleteCache(cacheKey)
        return cache
    }


}