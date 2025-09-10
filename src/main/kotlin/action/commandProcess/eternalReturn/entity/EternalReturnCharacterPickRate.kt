package cn.luorenmu.action.commandProcess.eternalReturn.entity

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.07.31 9:07
 */
@Serializable
data class EternalReturnCharacterPickRate(
    val characterId: Int,
    val pickRate: Double = 0.0,
)
