package cn.luorenmu.action.commandProcess.eternalReturn.entity.item

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2025.04.14 20:23
 */
@Serializable
data class EternalReturnItemInfos(
    val items: List<Item>,
) {
    @Serializable
    data class Item(
        val id: Long = 0,
        val name: String = "",
        val tooltip: String = "",
        val imageUrl: String = "",
        val type: String = "",
        val miscItemType: String? = null,
        val grade: String = "",
        val spawnAreas: List<Long>? = null,
        val weaponType: String? = null,
        val makeMaterial1: Long? = null,
        val makeMaterial2: Long? = null,
        val makeMaterials: List<Long>? = null,
        val armorType: String? = null,
        val consumableType: String? = null,
        val consumableTag: String? = null,
        val specialItemType: String? = null,
    )
}


