package action.commandProcess.eternalReturn.entity

import kotlinx.serialization.Serializable


/**
 * @author LoMu
 * Date 2024.07.31 9:48
 */
@Serializable
data class EternalReturnSeason(
    val seasons: List<EternalReturnSeasons>,
) {
    val currentSeason: EternalReturnSeasons by lazy {
        seasons.first { it.current }
    }

}
