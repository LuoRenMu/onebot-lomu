package cn.luorenmu.action.commandProcess.eternalReturn

import cn.luorenmu.action.commandProcess.CommandProcess
import cn.luorenmu.action.render.EternalReturnFindPlayerRender
import cn.luorenmu.action.request.EternalReturnRequestData
import cn.luorenmu.action.webPageScreenshot.EternalReturnWebPageScreenshot
import cn.luorenmu.common.extensions.getFirstBot
import cn.luorenmu.common.extensions.sendMsg
import cn.luorenmu.config.shiro.customAction.setMsgEmojiLike
import cn.luorenmu.listen.entity.MessageSender
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.BotContainer
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

/**
 * @author LoMu
 * Date 2025.01.28 13:42
 */
@Component("eternalReturnFindPlayers")
class EternalReturnFindPlayer(
    private val eternalReturnRequestData: EternalReturnRequestData,
    private val eternalReturnWebPageScreenshot: EternalReturnWebPageScreenshot,
    private val botContainer: BotContainer,
    private val eternalReturnFindPlayerRender: EternalReturnFindPlayerRender,
) : CommandProcess {

    override fun process(sender: MessageSender): String? {
        var nickname = sender.originalMessage(command())


        // check name rule
        if (nickname.isBlank()) {
            nickname = sender.senderName
        }
        if (nickname.contains("@") || nickname.length < 2) {
            return MsgUtils.builder().text("名称不合法 -> $nickname").build()
        }

        return runBlocking {
            if (!eternalReturnRequestData.syncPlayers(nickname)) {
                return@runBlocking MsgUtils.builder().text("不存在的玩家 -> $nickname").build()
            }
            botContainer.getFirstBot().setMsgEmojiLike(sender.messageId.toString(), "124")
            try {
                return@runBlocking eternalReturnWebPageScreenshot.webPlayerPageScreenshot(nickname)
            } catch (_: Exception) {
                botContainer.getFirstBot()
                    .sendMsg(
                        sender.messageType,
                        sender.groupOrSenderId,
                        MsgUtils.builder().reply(sender.messageId).text("与服务器无法正常连接 正在重试")
                            .build()
                    )
                return@runBlocking eternalReturnFindPlayerRender.imageRenderGenerate(nickname)
            }
        }
    }


    override fun commandName(): String {
        return "永恒轮回网页查询玩家"
    }

    override fun state(id: Long): Boolean {
        return true
    }

    override fun command(): Regex = Regex("^网页查询")

    override fun needAtBot(): Boolean = false
}