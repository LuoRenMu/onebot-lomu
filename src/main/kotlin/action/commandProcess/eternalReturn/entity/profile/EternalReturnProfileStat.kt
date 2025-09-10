package action.commandProcess.eternalReturn.entity.profile

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.08.03 14:20
 */
@Serializable
data class EternalReturnProfileStat(
    val key: Long = 0,
    val updatedAt: Long? = null,
    val play: Int = 0,
    val win: Long = 0,
    val top2: Long = 0,
    val top3: Long = 0,
    val place: Long = 0,
    val playerKill: Long = 0,
    val playerAssistant: Long = 0,
    val teamKill: Long = 0,
    val monsterKill: Long = 0,
    val damageToPlayer: Int = 0,
    val damageToMonster: Long = 0,
    val mmrGain: Int = 0,
    val playTime: Long = 0,
    val playerDeaths: Long = 0,
    val weaponStats: List<EternalReturnProfileStat>? = null,
    val skinStats: List<EternalReturnProfileStat>? = null,
)
