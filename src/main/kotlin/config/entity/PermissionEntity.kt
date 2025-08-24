package cn.luorenmu.config.entity

/**
 *
 * @author LoMu
 * Date 2025/8/24 19:06
 */
data class PermissionEntity(
    val permission: MutableList<PermissionUser> = mutableListOf(),
) {
    data class PermissionUser(
        val qq: Long = 0,
        val role: String = "",
    )
}
