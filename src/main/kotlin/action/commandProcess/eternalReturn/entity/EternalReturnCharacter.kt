package action.commandProcess.eternalReturn.entity

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.07.31 21:53
 */
@Serializable
data class EternalReturnCharacter(
    val characters: ArrayList<EternalReturnCharacterById> = arrayListOf(),
)
