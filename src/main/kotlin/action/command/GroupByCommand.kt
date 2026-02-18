package cn.luorenmu.action.command

import cn.luorenmu.action.commandProcess.CommandProcess
import cn.luorenmu.common.extensions.getAtQQAll
import cn.luorenmu.common.extensions.getFirstBot
import cn.luorenmu.listen.entity.MessageSender
import cn.luorenmu.service.GroupByService
import com.mikuac.shiro.core.BotContainer
import org.springframework.stereotype.Component

/**
 *
 * @author LoMu
 * Date 2026/2/3 10:21
 */
@Component
class GroupByCommand(
    private val groupByService: GroupByService,
    private val botContainer: BotContainer,
) : CommandProcess {
    override suspend fun process(sender: MessageSender): String? {
        val split = sender.message.split(" ")
        if (split.size >= 2) {
            val ats = split.drop(2).joinToString { it }
            val atQQAll = ats.getAtQQAll()
            if (atQQAll.isEmpty()) {
                return null
            }
            groupByService.addAll(atQQAll, split[1])
            botContainer.getFirstBot().setMsgEmojiLike(sender.messageId, "124", true)
        }
        return null
    }

    override fun commandName(): String {
        return "group"
    }

    override fun state(id: Long): Boolean {
        return true
    }

    override fun command(): Regex = "^分组".toRegex()

    override fun needAtBot(): Boolean = false
}