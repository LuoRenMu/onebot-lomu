package action.commandProcess.eternalReturn.entity

import action.commandProcess.eternalReturn.entity.tier.EternalReturnTierDistributionDtos
import cn.luorenmu.action.commandProcess.eternalReturn.entity.EternalReturnCutoffs
import cn.luorenmu.action.commandProcess.eternalReturn.entity.EternalReturnPlayerTierByUserNum
import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.07.31 8:59
 */
@Serializable
data class EternalReturnLeaderboard(
    val cutoffs: ArrayList<EternalReturnCutoffs>,
    val leaderboards: ArrayList<EternalReturnLeaderboardPlayer>,
    val playerTierByUserNum: HashMap<Int, EternalReturnPlayerTierByUserNum>,
    val tierDistributionDtos: ArrayList<EternalReturnTierDistributionDtos>,
    val totalLeaderBoardCount: Int,
    val updatedAt: Long,
    var currentSeason: EternalReturnSeason? = null,
)


