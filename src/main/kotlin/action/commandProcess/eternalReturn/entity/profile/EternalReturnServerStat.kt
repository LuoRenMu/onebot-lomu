package action.commandProcess.eternalReturn.entity.profile

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.08.03 14:23
 */
@Serializable
data class EternalReturnServerStat(
    val key: String = "",
    val updatedAt: Long = 0,
    val play: Long = 0,
)
