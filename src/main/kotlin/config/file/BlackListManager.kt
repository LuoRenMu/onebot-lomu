package cn.luorenmu.config.file

import cn.luorenmu.config.entity.BlackListEntity
import cn.luorenmu.listen.entity.BotRole
import cn.luorenmu.listen.entity.MessageSender
import cn.luorenmu.listen.entity.MessageType
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * @author LoMu
 * Date 2025.03.21 22:38
 */

/**
 * 黑白名单管理工具
 * 支持用户和群组的黑白名单模式
 */
object BlackListManager {

    private const val FILE_NAME = "black_list.json"
    private val logger = KotlinLogging.logger {}


    val blackListData: BlackListEntity by lazy {
        ConfigFile.loadFile(FILE_NAME, BlackListEntity())
    }


    fun isAccessAllowedPrivate(userId: Long): Boolean =
        blackListData.userIsWhite == blackListData.userList.contains(userId)

    fun isAccessAllowedGroup(groupId: Long): Boolean =
        blackListData.groupIsWhite == blackListData.groupList.contains(groupId)

    /**
     * 检查消息发送者权限
     * @return true 表示允许，false 表示应拒绝
     */
    fun checkBlackList(sender: MessageSender, reject: (MessageType) -> Unit = {}): Boolean {

        if (sender.role.roleNumber >= BotRole.ADMIN.roleNumber) return true

        return when (sender.messageType) {
            MessageType.GROUP -> {
                if (!isAccessAllowedPrivate(sender.senderId)) {
                    reject(MessageType.PRIVATE)
                    return false
                }
                if (!isAccessAllowedGroup(sender.groupOrSenderId)) {
                    reject(MessageType.GROUP)
                    return false
                }
                true
            }

            MessageType.PRIVATE -> {
                if (!isAccessAllowedPrivate(sender.senderId)) {
                    reject(MessageType.PRIVATE)
                    return false
                }
                true
            }
        }
    }


    fun saveToFile() {
        synchronized(blackListData) {
            ConfigFile.updateFile(FILE_NAME, blackListData)
        }
        logger.info { "黑白名单文件已更新: $FILE_NAME" }
    }


}