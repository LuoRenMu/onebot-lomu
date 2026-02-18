package cn.luorenmu.action.command

import cn.luorenmu.action.commandProcess.CommandProcess
import cn.luorenmu.listen.entity.MessageSender
import cn.luorenmu.service.GroupByService
import com.mikuac.shiro.common.utils.MsgUtils
import org.springframework.stereotype.Component

/**
 *
 * @author LoMu
 * Date 2026/2/3 10:07
 */
@Component
class GroupByAtCommand(
    private val groupByService: GroupByService,
) : CommandProcess {
    override suspend fun process(sender: MessageSender): String? {
        val spilt = sender.message.split(" ")
        val string = spilt[1]
        groupByService.get(string)?.let {
            if (it.isNotEmpty()) {
                val sb = StringBuilder()
                for (lng in it) {
                    sb.append(MsgUtils.builder().at(lng).build())
                }
                return sb.toString()
            }
        }
        return null
    }

    override fun commandName(): String {
        return "group at"
    }

    override fun state(id: Long): Boolean {
        return true
    }

    override fun command(): Regex = "^at".toRegex()
    override fun needAtBot(): Boolean = false
}
