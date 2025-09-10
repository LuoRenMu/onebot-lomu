package action.commandProcess.eternalReturn.entity.tier

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.07.31 9:25
 */
// 段位图 >= 无暇
@Serializable
data class EternalReturnTierDistributionDtos(
    val count: Int = 0,
    val rate: Double = 0.0,
    val tierGrade: Int = 0,
    val tierImageUrl: String = "",
    val tierType: Int = 0,
)
