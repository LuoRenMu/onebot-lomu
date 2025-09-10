package action.commandProcess.eternalReturn.entity.profile

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.08.03 14:31
 */
@Serializable
data class EternalReturnProfilePlayerSeason(
    val seasonId: Int = 0,
    var mmr: Int = 0,
    var tierId: Int = 0,
    var tierGradeId: Long = 0,
    var tierMmr: Long = 0,
)
