package cn.luorenmu.action.commandProcess.eternalReturn.entity.skill

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2025.04.10 18:03
 */
@Serializable
data class EternalReturnTacticalSkill(
    val tacticalSkills: List<EternalReturnSkill>,
) {
    @Serializable
    data class EternalReturnSkill(
        val id: Long = 0,
        val name: String = "",
        val tooltip: String = "",
        val imageUrl: String = "",
    )
}