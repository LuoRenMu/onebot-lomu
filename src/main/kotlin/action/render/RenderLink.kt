package cn.luorenmu.action.render

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 *
 * @author LoMu
 * Date 2025/9/3 23:27
 */
@Component
class RenderLink(
    @Value("\${server.port}")
    private val port: String,
)