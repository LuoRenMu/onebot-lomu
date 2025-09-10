package cn.luorenmu.action.commandProcess.eternalReturn.entity.matcher

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2025.06.24 13:48
 */
@Serializable
data class EternalReturnMatchesById(
    val matches: List<EternalReturnMatches.Match> = listOf(),
    val playerTiers: List<EternalReturnPlayerTier> = listOf(),
) {
    @Serializable
    data class EternalReturnPlayerTier(
        val tierId: Int = 0,
        val tierGradeId: Long = 0,
        val tierMmr: Long = 0,
        val mmr: Int = 0,
        val userNum: Int = 0,
    )
}
