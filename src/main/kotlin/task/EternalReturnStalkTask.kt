package cn.luorenmu.task

import cn.luorenmu.action.render.EternalReturnFindPlayerRender
import cn.luorenmu.action.request.EternalReturnRequestData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 *
 * @author LoMu
 * Date 2025/9/12 14:08
 */
@Component
class EternalReturnStalkTask(
    private val eternalReturnFindPlayerRender: EternalReturnFindPlayerRender,
    private val requestData: EternalReturnRequestData,
) {
    private val log = KotlinLogging.logger {}

    @Scheduled(initialDelay = 60 * 1000, fixedDelay = 3 * 60 * 1000)
    fun stalk() {

    }
}