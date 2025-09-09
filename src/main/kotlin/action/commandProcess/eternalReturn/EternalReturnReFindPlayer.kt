package cn.luorenmu.action.commandProcess.eternalReturn

import cn.luorenmu.action.commandProcess.CommandProcess
import cn.luorenmu.action.render.EternalReturnFindPlayerRender
import cn.luorenmu.action.request.EternalReturnRequestData
import cn.luorenmu.common.extensions.getFirstBot
import cn.luorenmu.config.entity.AliasNameListEntity
import cn.luorenmu.config.file.EternalReturnAliasName
import cn.luorenmu.config.shiro.customAction.setMsgEmojiLike
import cn.luorenmu.listen.entity.MessageSender
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.BotContainer
import org.springframework.stereotype.Component

/**
 * @author LoMu
 * Date 2025.01.28 14:28
 */
@Component("eternalReturnReFindPlayers")
class EternalReturnReFindPlayer(
    private val eternalReturnFindPlayerRender: EternalReturnFindPlayerRender,
    private val botContainer: BotContainer,
    private val eternalReturnRequestData: EternalReturnRequestData,
) : CommandProcess {

    private val playerNames: AliasNameListEntity = EternalReturnAliasName.getPlayerNickName()
    override fun process(sender: MessageSender): String? {
        var nickname = sender.originalMessage(command())
        for (player in playerNames.aliasNames) {
            player.alias.firstOrNull { it == nickname }?.let {
                nickname = player.nickname
            }
        }
        if (!eternalReturnRequestData.syncPlayers(nickname)) {
            return MsgUtils.builder().text("不存在的玩家 -> $nickname").build()
        }
        if (nickname.contains("@") || nickname.length < 2) {
            return MsgUtils.builder().text("名称不合法 -> $nickname").build()
        }

        botContainer.getFirstBot().setMsgEmojiLike(sender.messageId.toString(), "124")
        return eternalReturnFindPlayerRender.imageRenderGenerate(nickname)
    }

    override fun commandName(): String {
        return "永恒轮回查询玩家"
    }

    override fun state(id: Long): Boolean {
        return true
    }

    override fun command(): Regex = Regex("^(((玩家|战绩)查询)|(search)|(查询玩家)|(查询战绩))")

    override fun needAtBot(): Boolean = false
}