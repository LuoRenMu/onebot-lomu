package cn.luorenmu.action.commandProcess

import cn.luorenmu.common.extensions.getFirstBot
import cn.luorenmu.common.extensions.isCQReply
import cn.luorenmu.common.extensions.sendMsg
import cn.luorenmu.exception.LoMuBotException
import cn.luorenmu.listen.entity.MessageSender
import cn.luorenmu.listen.entity.MessageType
import com.microsoft.playwright.TimeoutError
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotContainer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

/**
 * @author LoMu
 * Date 2024.07.13 1:52
 */
@Component
class OneBotCommandRouter(
    applicationContext: ApplicationContext,
    private val bot: BotContainer,
) {
    private val log = KotlinLogging.logger {}

    private val commandList: List<CommandProcess> =
        applicationContext.getBeansOfType<CommandProcess>().values.toList()

    private fun isCurrentCommand(
        botId: Long,
        command: String,
        oneBotCommand: CommandProcess,
    ): Boolean {
        val atMe = MsgUtils.builder().at(botId).build()
        // 去除@ 和空字符
        var originMessage = command.replace(atMe, "")
        if (oneBotCommand.needAtBot()) {
            if (!command.contains(atMe)) {
                return false
            }
        }

        // 移除回复
        if (originMessage.isCQReply()) {
            originMessage =
                originMessage.replace("\\[CQ:reply,id=\\d+]".toRegex(), "")
        }

        return originMessage.matches(oneBotCommand.command())
    }

    private fun send(message: String?, id: Long, messageId: Int, type: MessageType) {
        message?.let {
            if (it.isNotBlank()) {
                bot.getFirstBot().sendMsg(
                    type,
                    id,
                    MsgUtils.builder().reply(messageId).text(it).build(),
                )
            }
        }
    }


    fun process(bot: Bot, messageSender: MessageSender) {
        val botId = bot.selfId
        commandList.firstOrNull { isCurrentCommand(botId, messageSender.message, it) }
            ?.let { oneBotCommand ->
                try {
                    send(
                        oneBotCommand.process(messageSender),
                        messageSender.groupOrSenderId,
                        messageSender.messageId,
                        messageSender.messageType
                    )
                } catch (e: LoMuBotException) {
                    send(
                        e.msg,
                        messageSender.groupOrSenderId,
                        messageSender.messageId,
                        messageSender.messageType
                    )
                } catch (e: TimeoutError) {
                    log.error { "${oneBotCommand.commandName()}:页面长时间加载仍未完成,为保证后续任务仍然执行 该任务已被强行中断 ${e.stackTraceToString()}" }
                    send(
                        "执行时间过长,已被强行中断",
                        messageSender.groupOrSenderId,
                        messageSender.messageId,
                        messageSender.messageType
                    )
                } catch (e: Exception) {
                    log.error { "${oneBotCommand.commandName()}:${e.stackTraceToString()}" }
                    send(
                        "服务器内部错误",
                        messageSender.groupOrSenderId,
                        messageSender.messageId,
                        messageSender.messageType
                    )
                }
            }
    }
}
