package action.commandProcess.eternalReturn.entity

import cn.luorenmu.action.commandProcess.eternalReturn.entity.EternalReturnCharacterPickRate
import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.07.31 9:05
 */
@Serializable
data class EternalReturnLeaderboardPlayer(
    val avgPlacement: Double = 0.0,
    val avgPlayerKill: Double = 0.0,
    val characterIds: ArrayList<Int>? = null,
    val mmr: Int = 0,
    val mostCharacters: ArrayList<EternalReturnCharacterPickRate> = arrayListOf(),
    val nickname: String = "螺母",
    val playCount: Int = 0,
    val rank: Int = 0,
    val rankDiff: Int = 0,
    val top3Rate: Double = 0.0,
    val userNum: Long = 0,
    val winRate: Double = 0.0,
)
