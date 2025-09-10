package cn.luorenmu.action.commandProcess.eternalReturn.entity.tier

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2025.03.29 21:22
 */
/**
 * 段位信息
 */
@Serializable
data class EternalReturnTiers(
    val tiers: ArrayList<EternalReturnTier> = arrayListOf(),
) {
    @Serializable
    data class EternalReturnTier(
        val id: Int = 0,
        val key: String = "",
        val name: String = "",
        val imageUrl: String = "",
        val iconUrl: String = "",
    )
}
