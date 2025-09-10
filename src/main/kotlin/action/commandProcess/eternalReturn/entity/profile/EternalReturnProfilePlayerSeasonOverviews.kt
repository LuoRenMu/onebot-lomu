package action.commandProcess.eternalReturn.entity.profile

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.08.03 14:12
 */

/**
 * 赛季概括
 */
@Serializable
data class EternalReturnProfilePlayerSeasonOverviews(
    val userNum: Long = 0,
    val seasonID: Long = 0,
    // 3为排位模式，2为匹配模式 6为钴协议 0为全部
    val matchingModeId: Int = 0,
    val teamModeId: Int = 0,
    val updatedAt: Long = 0,
    val mmr: Int = 0,
    val play: Int = 0,
    val win: Int = 0,
    val top2: Int = 0,
    val top3: Int = 0,
    val place: Int = 0,
    val playerKill: Int = 0,
    val playerAssistant: Int = 0,
    val teamKill: Int = 0,
    val monsterKill: Int = 0,
    val damageToPlayer: Int = 0,
    val damageToMonster: Int = 0,
    val mmrGain: Int = 0,
    val playTime: Long = 0,
    val playerDeaths: Int = 0,
    val characterStats: List<EternalReturnProfileStat>,
    val serverStats: List<EternalReturnServerStat>,
    val mmrStats: List<List<Int>>,
    val duoStats: List<EternalReturnProfileDuoStat>,
    val recentMatches: List<RecentGameMatcher>,
    var tierId: Long? = null,
    var tierGradeId: Long? = null,
    var tierMmr: Long? = null,
    var rank: EternalReturnProfileRank? = null,
) {
    @Serializable
    data class RecentGameMatcher(
        val gameId: Long = 0,
        val seasonId: Int = 0,
        // 3为排位模式，2为匹配模式 6为钴协议 0为全部
        val matchingMode: Int = 0,
        val teamMode: Int = 0,
        val characterNum: Int = 0,
        val skinCode: Int = 0,
        val gameRank: Int = 0,
        val playerKill: Int = 0,
        val playerAssistant: Int = 0,
        val monsterKill: Int = 0,
        val bestWeapon: Int = 0,
        val mmrGain: Int = 0,
        val preMade: Int = 0,
        val damageToPlayer: Int = 0,
        val damageToMonster: Int = 0,
        val giveUp: Int = 0,
        val teamKill: Int = 0,
        val playerDeaths: Int = 0,
        val escapeState: Int = 0,
    )
}
