package action.commandProcess.eternalReturn.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.08.05 7:18
 */

/**
 *     {
 *       "id": 30,
 *       "key": "PRE_SEASON_16",
 *       "name": "季前赛 7"
 *     },
 *     {
 *       "id": 31,
 *       "key": "SEASON_16",
 *       "name": "正式赛季 S7",
 *       "current": true
 *     }
 */
@Serializable
data class EternalReturnSeasons(
    val id: Int,
    val key: String,
    val name: String,
    @SerialName("isCurrent")
    val current: Boolean = false,
)
