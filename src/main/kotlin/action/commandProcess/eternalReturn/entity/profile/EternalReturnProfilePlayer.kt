package action.commandProcess.eternalReturn.entity.profile

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.08.03 14:10
 */
@Serializable
data class EternalReturnProfilePlayer(
    val accountLevel: Int = 0,
    val lastPlayedSeasonId: Int = 0,
    val name: String = "",
    val syncedAt: Long = 0,
    val userNum: Long = 0,
)
