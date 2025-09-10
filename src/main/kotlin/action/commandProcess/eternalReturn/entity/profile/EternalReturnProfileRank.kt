package action.commandProcess.eternalReturn.entity.profile

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.08.03 14:26
 */
/**
 *  高手 进入了 前1000名
 */
@Serializable
data class EternalReturnProfileRank(
    val in1000: EternalReturnProfileRankGlobal? = null,
    val local: EternalReturnProfileRankGlobal? = null,
    val global: EternalReturnProfileRankGlobal? = null,
)