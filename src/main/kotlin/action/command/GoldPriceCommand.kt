package cn.luorenmu.action.command

import cn.luorenmu.action.commandProcess.CommandProcess
import cn.luorenmu.action.request.HTTPRequestUtil
import cn.luorenmu.listen.entity.MessageSender
import io.ktor.client.statement.*
import org.springframework.stereotype.Component

/**
 *
 * @author LoMu
 * Date 2026/2/2 09:46
 */
@Component
class GoldPriceCommand : CommandProcess {
    override suspend fun process(sender: MessageSender): String? {
        val jsCode = HTTPRequestUtil.call("https://www.huilvbiao.com/api/gold_indexApi").bodyAsText()
        val pattern = """var\s+hq_str_(\w+)\s*=\s*"([^"]+)";""".toRegex()
        val sb = StringBuilder()
        pattern.findAll(jsCode).forEach { match ->
            val symbol = match.groupValues[1]
            val dataStr = match.groupValues[2]
            val fields = dataStr.split(',')

            val firstPrice = fields.getOrNull(0)?.toDoubleOrNull()
            val date = fields.findLast { it.matches(Regex("""\d{4}-\d{2}-\d{2}""")) } ?: "未知日期"
            val address = fields.asReversed().firstOrNull { it.isNotBlank() && it != "0" } ?: "未知地区"
            sb.append("[$symbol] 价格: $firstPrice | 日期: $date | 地区: $address\n")
        }
        return sb.toString()
    }

    override fun commandName(): String = "gold"

    override fun state(id: Long): Boolean = true

    override fun command(): Regex = "^金价$".toRegex()

    override fun needAtBot() = false
}