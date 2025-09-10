package cn.luorenmu.action.commandProcess.eternalReturn.entity.weapon

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2025.04.15 18:03
 */
@Serializable
data class EternalReturnWeapons(
    val masteries: List<EternalReturnWeapon> = listOf(),
) {
    @Serializable
    data class EternalReturnWeapon(
        val id: Int = 0,
        val key: String = "",
        val name: String = "",
        val iconUrl: String = "",
    )
}
