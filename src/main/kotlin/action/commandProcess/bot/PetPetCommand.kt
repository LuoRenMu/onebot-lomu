package cn.luorenmu.action.commandProcess.bot

import cn.luorenmu.action.commandProcess.CommandProcess
import cn.luorenmu.action.petpet.PetPetGenerate
import cn.luorenmu.action.petpet.TemplateRegister
import cn.luorenmu.action.request.QQRequestData
import cn.luorenmu.common.extensions.*
import cn.luorenmu.config.file.PermissionsManager
import cn.luorenmu.config.shiro.customAction.getImage
import cn.luorenmu.listen.entity.BotRole
import cn.luorenmu.listen.entity.MessageSender
import cn.luorenmu.listen.entity.MessageType
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.BotContainer
import com.mikuac.shiro.dto.action.response.GetMsgResp
import org.springframework.stereotype.Component

/**
 *
 * @author LoMu
 * Date 2025/8/25 02:13
 */
@Component
class PetPetCommand(
    private val emojiGenerationCommand: EmojiGenerationCommand,
    private val botContainer: BotContainer,
    private val qqRequestData: QQRequestData,
) : CommandProcess {
    override fun process(sender: MessageSender): String? {
        if (!(sender.role.roleNumber >= BotRole.GroupAdmin.roleNumber ||
                    emojiGenerationCommand.state(sender.groupOrSenderId))
        ) {
            return null
        }

        val templateName =
            sender.message.replace("/", "").replaceAtToEmpty().replaceBlankToEmpty()
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
                return qqRequestData.downloadQQAvatar(it)
            }
            return qqRequestData.downloadQQAvatar(messageSender.senderId.toString())
        }
        // 回复消息
        messageSender.message.getCQReplyMessageId()?.let {
            val msg = botContainer.getFirstBot().getMsg(it.toInt()).data
            return triggerObj(getMsgToMessageSender(msg, messageSender), index, existsFrom = true)
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
            return qqRequestData.downloadQQAvatar(it)
        }

        return qqRequestData.downloadQQAvatar(messageSender.senderId.toString())
    }


    private fun getMsgToMessageSender(msg: GetMsgResp, messageSender: MessageSender) = MessageSender(
        groupOrSenderId = messageSender.groupOrSenderId,
        senderName = msg.sender.nickname,
        senderId = msg.sender.userId.toLong(),
        role = PermissionsManager.botRole(msg.sender.userId.toLong(), msg.sender.role),
        message = msg.message,
        messageType = MessageType.convert(msg.messageType),
        messageId = msg.messageId,
        botId = messageSender.botId
    )

    override fun commandName(): String = "PetPet"


    override fun state(id: Long): Boolean = true

    override fun command() = Regex("^/")


    override fun needAtBot(): Boolean = false

}