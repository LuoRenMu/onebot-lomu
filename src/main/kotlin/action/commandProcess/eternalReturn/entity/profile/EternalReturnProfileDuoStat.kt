package action.commandProcess.eternalReturn.entity.profile

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.08.03 14:25
 */
@Serializable
data class EternalReturnProfileDuoStat(
    val userNum: Long,
    val nickname: String,
    val updatedAt: Long,
    val play: Int,
    val win: Int,
    val place: Int,
    val characterStats: List<EternalReturnProfileCharacterStat>,
)
