package action.commandProcess.eternalReturn.entity.profile

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.08.03 14:28
 */
@Serializable
data class EternalReturnProfileRankGlobal(
    val rank: Long = 0,
    val rankSize: Long = 0,
)

