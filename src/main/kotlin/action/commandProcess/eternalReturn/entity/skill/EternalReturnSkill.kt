package cn.luorenmu.action.commandProcess.eternalReturn.entity.skill

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2025.04.10 18:02
 */
@Serializable
data class EternalReturnSkill(
    val key: String = "",
    val name: String = "",
    val tooltip: String = "",
    val imageUrl: String = "",
)
