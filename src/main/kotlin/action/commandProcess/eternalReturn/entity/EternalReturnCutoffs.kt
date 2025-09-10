package cn.luorenmu.action.commandProcess.eternalReturn.entity

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.07.31 9:03
 */
@Serializable
data class EternalReturnCutoffs(
    val mmr: Int = 0,
    val teamModeId: Int = 0,
    val tierType: Int = 0,
)
