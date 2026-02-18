package cn.luorenmu.action.command

import cn.luorenmu.action.commandProcess.CommandProcess
import cn.luorenmu.action.petpet.PetPetGenerate
import cn.luorenmu.action.petpet.TemplateRegister
import cn.luorenmu.action.request.QQRequestData
import cn.luorenmu.common.extensions.*
import cn.luorenmu.config.shiro.customAction.getImage
import cn.luorenmu.listen.entity.BotRole
import cn.luorenmu.listen.entity.MessageSender
import cn.luorenmu.listen.entity.MessageType
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.BotContainer
import com.mikuac.shiro.dto.action.response.MsgResp
import org.springframework.stereotype.Component

/**
 *
 * @author LoMu
 * Date 2025/8/25 02:13
 */
@Component
class PetPetCommand(
    private val botContainer: BotContainer,
    private val qqRequestData: QQRequestData,
) : CommandProcess {
    override suspend fun process(sender: MessageSender): String? {
        val templateName =
            sender.message.replaceAtToEmpty().replaceBlankToEmpty()
                .replaceReplyToEmpty().replaceImageToEmpty()
        TemplateRegister.getTemplate(templateName)?.let {
            val existsFrom = it.elements.toString().contains("from")
            val triggerTo = triggerObj(sender, 0, existsFrom)
            val triggerFrom = triggerObj(sender, 1, existsFrom)
            val path =
                PetPetGenerate.generate(
                    it,
                    triggerTo,
                    triggerFrom,
                    sender.message.replaceAtToEmpty().replaceReplyToEmpty()
                )

            botContainer.getFirstBot()
                .sendMsg(sender.messageType, sender.groupOrSenderId, MsgUtils.builder().img(path).build())
        }
        return null
    }

    /**
     *  @param index to表示0 from表示1
     *  @return 如果为null表示触发对象为机器人
     */
    private fun triggerObj(
        messageSender: MessageSender,
        index: Int,
        existsFrom: Boolean,
    ): String {
        // 模版存在from并且当前为from的情况下 则是第一条at或第二条at
        if (existsFrom && index == 1) {
            messageSender.message.getAtQQ(1)?.let {
                return qqRequestData.getAvatarUrlString(it)
            }
            return qqRequestData.getAvatarUrlString(messageSender.senderId.toString())
        }
        // 回复消息
        messageSender.message.getCQReplyMessageId()?.let {
            val msg = botContainer.getFirstBot().getMsg(it.toInt()).data
            return triggerObj(getMsgToMessageSender(msg, it.toInt(), messageSender), index, existsFrom = true)
        }

        // 自己发送的图片
        messageSender.message.getCQFileStr()?.let {
            return botContainer.getFirstBot().getImage(it).data.file
        }
        // 回复的图片
        messageSender.message.getFileStr()?.let {
            return botContainer.getFirstBot().getImage(it).data.file
        }


        // 当前消息为to为at的目标或自己
        messageSender.message.getAtQQ(0)?.let {
            return qqRequestData.getAvatarUrlString(it)
        }

        return qqRequestData.getAvatarUrlString(messageSender.senderId.toString())
    }


    private fun getMsgToMessageSender(msg: MsgResp, msgId: Int, messageSender: MessageSender) = MessageSender(
        groupOrSenderId = messageSender.groupOrSenderId,
        senderName = msg.sender.nickname,
        senderId = msg.sender.userId.toLong(),
        role = BotRole.Member,
        message = msg.message,
        messageType = MessageType.convert(msg.messageType),
        messageId = msgId,
        botId = messageSender.botId
    )

    override fun commandName(): String = "表情包生成"


    override fun state(id: Long): Boolean = true

    override fun command() = Regex(TemplateRegister.petPetTemplates.keys.joinToString("|", "^", "$") { it })


    override fun needAtBot(): Boolean = false

}