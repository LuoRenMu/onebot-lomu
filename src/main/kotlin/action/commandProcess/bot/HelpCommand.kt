package cn.luorenmu.action.commandProcess.bot

import cn.luorenmu.action.commandProcess.CommandProcess
import cn.luorenmu.listen.entity.MessageSender
import org.springframework.stereotype.Component

/**
 * @author LoMu
 * Date 2025.01.30 16:39
 */
@Component("HelpCommand")
class HelpCommand : CommandProcess {

    companion object {
        const val HELP_LINK = "LoMu-Bot使用教程 https://docs.qq.com/doc/DQnpKbnhsRkx5UFd4"
    }

    override fun process(sender: MessageSender): String? {
        return HELP_LINK
    }

    override fun commandName() = "HelpCommand"

    override fun state(id: Long) = true
    override fun command(): Regex = Regex("^((/help)|(帮助))$")

    override fun needAtBot(): Boolean = false

}