package cn.luorenmu.listen

import cn.luorenmu.action.commandProcess.OneBotCommandRouter
import cn.luorenmu.listen.entity.BotRole
import cn.luorenmu.listen.entity.MessageSender
import cn.luorenmu.listen.entity.MessageType
import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import org.springframework.stereotype.Component


/**
 * @author LoMu
 * Date 2024.07.04 10:22
 */


@Component
@Shiro
class GroupEventListen(
    private val oneBotCommandRouter: OneBotCommandRouter,
) {

    @GroupMessageHandler
    fun groupMsgListen(bot: Bot, groupMessageEvent: GroupMessageEvent) {
        val groupId = groupMessageEvent.groupId
        val sender = groupMessageEvent.sender
        val senderId = sender.userId
        // 替换掉群备注 [CQ:at,qq=141412312,name=群最帅] -> [CQ:at,qq=141412312)
        val message = groupMessageEvent.message.replace(Regex("""(\[CQ:at,qq=\d+),name=[^,\]]*"""), "$1")


        val messageSender = MessageSender(
            groupId,
            sender.nickname,
            senderId,
            BotRole.convert(sender.role),
            groupMessageEvent.messageId,
            message,
            MessageType.GROUP,
            bot.selfId
        )

        // 指令
        oneBotCommandRouter.process(bot, messageSender)
    }
}