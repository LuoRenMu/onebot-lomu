package cn.luorenmu.action.render

import cn.luorenmu.action.commandProcess.eternalReturn.entity.dto.EternalReturnRender
import cn.luorenmu.common.utils.FreeMarkerUtils
import cn.luorenmu.common.utils.PathUtils
import cn.luorenmu.common.utils.WebPool
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author LoMu
 * Date 2025/9/10 14:40
 */
@Component
class FTLHRender(
    @Value("\${server.port}") private val port: String,
    private val webPool: WebPool,
) {

    companion object {
        val FTLHData = ConcurrentHashMap<String, String>()
        const val FIND_PLAYER = "eternal_return_player.ftlh"
    }

    fun generateEternalReturnFindPlayerFTLHImage(render: EternalReturnRender): String {
        val keyPrefix = "eternal_return_player_data_"
        val userNum = render.userNum
        val parseData = FreeMarkerUtils.parseData(FIND_PLAYER, render)
        FTLHData["ftlh:${keyPrefix}${userNum}"] = parseData
        val imgPath = PathUtils.getEternalReturnNicknameImagePath("render_$userNum")
        webPool.getWebPageScreenshot().screenshotSelector(
            "${urlPrefix()}/${keyPrefix}${userNum}", imgPath, "#content-container"
        )
        return imgPath
    }

    fun urlPrefix() = "http://localhost:$port/ftlh"
}