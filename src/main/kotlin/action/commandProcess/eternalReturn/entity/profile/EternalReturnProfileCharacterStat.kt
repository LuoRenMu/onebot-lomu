package action.commandProcess.eternalReturn.entity.profile

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.08.03 14:24
 */
@Serializable
data class EternalReturnProfileCharacterStat(
    val key: Long = 0,
    val updatedAt: Long = 0,
    val play: Long = 0,
)
