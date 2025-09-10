package cn.luorenmu.controller

import cn.luorenmu.action.render.FTLHRender
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
class FTLHController {

    @GetMapping("/ftlh/{id}")
    @ResponseBody
    fun getFtlh(@PathVariable id: String, httpResponse: HttpServletResponse): String {
        val cacheKey = "ftlh:$id"
        val cache = FTLHRender.FTLHData.remove(cacheKey)
            ?: run {
                httpResponse.status = 404
                return "404"
            }
        return cache
    }


}