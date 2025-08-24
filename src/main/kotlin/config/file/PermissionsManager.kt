package cn.luorenmu.config.file

import cn.luorenmu.config.entity.PermissionEntity
import cn.luorenmu.listen.entity.BotRole

/**
 * @author LoMu
 * Date 2024.09.10 09:00
 */

object PermissionsManager {

    private const val FILE_NAME = "permission.json"

    val permissionData by lazy {
        ConfigFile.loadFile(
            FILE_NAME, PermissionEntity(
                mutableListOf(
                    PermissionEntity.PermissionUser(0, "Admin"),
                    PermissionEntity.PermissionUser(1, "Owner")
                )
            )
        )
    }


    fun isOwner(id: Long): Boolean {
        return permissionData.permission.filter { it.role == "Owner" }.firstOrNull { it.qq == id } != null
    }

    fun isAdmin(id: Long): Boolean {
        return permissionData.permission.filter { it.role == "Admin" }.firstOrNull { it.qq == id } != null
    }

    fun botRole(sender: Long, role: String): BotRole {
        return when {
            isOwner(sender) -> BotRole.OWNER
            isAdmin(sender) -> BotRole.ADMIN
            else -> when (role.lowercase()) {
                "admin" -> BotRole.GroupAdmin
                "owner" -> BotRole.GroupOwner
                else -> BotRole.Member
            }
        }
    }
}