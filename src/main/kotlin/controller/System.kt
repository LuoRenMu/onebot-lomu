package cn.luorenmu.controller

import cn.luorenmu.action.render.EternalReturnFindPlayerRender
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


/**
 * @author LoMu
 * Date 2024.07.05 9:27
 */

@RestController
@RequestMapping("")
class System(
    private val eternalReturnRender: EternalReturnFindPlayerRender,
) {

    @GetMapping("/")
    fun system(): String {
        return "server running success"
    }

    @GetMapping("/search/{name}")
    fun search(@PathVariable name: String): String {
        return eternalReturnRender.imageRenderGenerate(name)
    }


}