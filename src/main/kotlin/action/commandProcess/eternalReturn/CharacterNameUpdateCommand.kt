package cn.luorenmu.action.commandProcess.eternalReturn

import cn.luorenmu.action.commandProcess.CommandProcess
import cn.luorenmu.config.entity.CharacterNickNameList
import cn.luorenmu.config.file.EternalReturnCharacterAliasName
import cn.luorenmu.listen.entity.BotRole
import cn.luorenmu.listen.entity.MessageSender
import org.springframework.stereotype.Component

/**
 * @author LoMu
 * Date 2025.06.11 12:17
 */
@Component
class CharacterNameUpdateCommand(
    private val characterNickName: CharacterNickNameList,
) : CommandProcess {


    override fun process(sender: MessageSender): String? {
        if (sender.role.roleNumber >= BotRole.ADMIN.roleNumber) {
            val newCharacterNickName = EternalReturnCharacterAliasName.getCharacterNickName()
            characterNickName.characterNickNames.clear()
            characterNickName.characterNickNames.addAll(newCharacterNickName.characterNickNames)
            return "已完成"
        }
        return null
    }

    override fun commandName(): String = "CharacterNameUpdate"

    override fun state(id: Long): Boolean = true

    override fun command(): Regex = Regex("^更新角色名称$")

    override fun needAtBot(): Boolean = true
}