package cn.luorenmu.action.commandProcess.eternalReturn.entity.skill

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2025.04.10 17:48
 * https://er.dakgg.io/api/v1/data/trait-skills?hl=zh-cn
 */
@Serializable
data class EternalReturnTraitSkills(
    val traitSkillGroups: List<EternalReturnSkill> = listOf(),
    val traitSkills: List<TraitSkill> = listOf(),
) {

    @Serializable
    data class TraitSkill(
        val id: Long = 0,
        val name: String = "",
        val tooltip: String = "",
        val group: String = "",
        val type: String = "",
        val imageUrl: String = "",
        val active: Boolean = false,
    )
}
