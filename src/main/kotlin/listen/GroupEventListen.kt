package cn.luorenmu.listen

import cn.luorenmu.action.PermissionsManager
import cn.luorenmu.action.commandProcess.OneBotCommandAllocator
import cn.luorenmu.action.listenProcess.BilibiliEventListen
import cn.luorenmu.action.listenProcess.PetpetListen
import cn.luorenmu.common.annotation.BlackList
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
    private val oneBotCommandAllocator: OneBotCommandAllocator,
    private val bilibiliEventListen: BilibiliEventListen,
    private val permissionsManager: PermissionsManager,
    private val petpetListen: PetpetListen,
) {

    @GroupMessageHandler
    @BlackList
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
            permissionsManager.botRole(senderId, sender.role),
            groupMessageEvent.messageId,
            message,
            MessageType.GROUP,
            bot.selfId
        )


        // 指令
        oneBotCommandAllocator.process(bot, messageSender)
        // 监听类
        bilibiliEventListen.process(bot, messageSender)
        petpetListen.process(messageSender)
    }
}