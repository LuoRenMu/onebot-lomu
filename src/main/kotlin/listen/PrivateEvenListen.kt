package cn.luorenmu.listen

import cn.luorenmu.action.commandProcess.OneBotCommandAllocator
import cn.luorenmu.listen.entity.BotRole
import cn.luorenmu.listen.entity.MessageSender
import cn.luorenmu.listen.entity.MessageType
import com.mikuac.shiro.annotation.PrivateMessageHandler
import com.mikuac.shiro.annotation.PrivateMsgDeleteNoticeHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.shiro.dto.event.notice.PrivateMsgDeleteNoticeEvent
import org.springframework.stereotype.Component

/**
 * @author LoMu
 * Date 2024.07.05 7:58
 */

@Component
@Shiro
class PrivateEvenListen(
    private val oneBotCommandAllocator: OneBotCommandAllocator,
) {

    @PrivateMessageHandler
    fun privateMessageHandler(bot: Bot, privateMessage: PrivateMessageEvent) {
        val messageSender = MessageSender(
            privateMessage.userId,
            "?",
            privateMessage.userId,
            BotRole.convert("user"),
            privateMessage.messageId,
            privateMessage.message,
            MessageType.PRIVATE,
            bot.selfId
        )
        oneBotCommandAllocator.process(bot, messageSender)
    }

    @PrivateMsgDeleteNoticeHandler
    fun privateDeleteHandler(bot: Bot, privateMessage: PrivateMsgDeleteNoticeEvent) {

    }
}