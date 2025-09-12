package cn.luorenmu.task

import cn.luorenmu.action.render.EternalReturnFindPlayerRender
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

/**
 *
 * @author LoMu
 * Date 2025/9/12 14:08
 */
@Component
class EternalReturnStalkTask(
    private val eternalReturnFindPlayerRender: EternalReturnFindPlayerRender,
) {
    private val log = KotlinLogging.logger {}

}