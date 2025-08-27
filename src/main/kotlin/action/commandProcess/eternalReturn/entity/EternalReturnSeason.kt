package action.commandProcess.eternalReturn.entity


/**
 * @author LoMu
 * Date 2024.07.31 9:48
 */
data class EternalReturnSeason(
    val seasons: List<EternalReturnSeasons>,
) {
    val currentSeason: EternalReturnSeasons by lazy {
        seasons.first { it.current }
    }

}
