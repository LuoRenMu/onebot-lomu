package action.commandProcess.eternalReturn.entity.tier

import action.commandProcess.eternalReturn.entity.EternalReturnDistributions
import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.08.03 9:41
 */

/**
 *  段位分布
 *  url -> https://dak.gg/er/statistics/tier?teamMode=SQUAD
 */
@Serializable
data class EternalReturnTierDistributions(
    val distributions: ArrayList<EternalReturnDistributions> = arrayListOf(),
    val updatedAt: Long = 0,
)