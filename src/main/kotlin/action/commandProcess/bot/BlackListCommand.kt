package cn.luorenmu.action.commandProcess.bot

import cn.luorenmu.action.commandProcess.CommandProcess
import cn.luorenmu.common.extensions.getAtQQ
import cn.luorenmu.common.extensions.isCQAt
import cn.luorenmu.config.file.BlackListManager
import cn.luorenmu.listen.entity.BotRole
import cn.luorenmu.listen.entity.MessageSender
import org.springframework.stereotype.Component

/**
 *
 * @author LoMu
 * Date 2025/8/19 00:48
 */
@Component
class BlackListCommand : CommandProcess {
    override fun process(sender: MessageSender): String? {
        if (sender.role.roleNumber < BotRole.ADMIN.roleNumber) {
            return null
        }
        try {
            val blackListData = BlackListManager.blackListData
            val originalMessage = sender.originalMessage(command())
            val numberStr = originalMessage.replace(command(), "")
            var number: Long = if (numberStr.isNotEmpty() && numberStr.matches(Regex("^\\d+$"))) {
                numberStr.toLong()
            } else {
                sender.groupOrSenderId
            }

            return if (sender.message.contains("群")) {
                blackListOper(number, blackListData.groupIsWhite, blackListData.groupList)
            } else {
                if (numberStr.isCQAt()) {
                    number = numberStr.getAtQQ()?.toLong() ?: return "未知 $numberStr"
                }
                blackListOper(number, blackListData.userIsWhite, blackListData.userList)
            }
        } finally {
            BlackListManager.saveToFile()
        }
    }

    fun blackListOper(id: Long, white: Boolean, list: MutableList<Long>): String {
        val contains = list.contains(id)
        val white = if (white) "白名单" else "黑名单"
        if (contains) {
            list.remove(id)
            return "$white 已移除 $id"
        } else {
            list.add(id)
            return "$white 已添加 $id"
        }
    }

    override fun commandName() = "黑白名单"


    override fun state(id: Long): Boolean = true

    override fun command() = Regex("^(黑名单群|黑名单QQ)")

    override fun needAtBot() = true
}