package cn.luorenmu.common.aop

import cn.luorenmu.common.aop.entity.BlackListData
import cn.luorenmu.file.ReadWriteFile
import com.alibaba.fastjson2.to
import com.alibaba.fastjson2.toJSONByteArray
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.MessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

/**
 * @author LoMu
 * Date 2025.03.21 22:38
 */
@Component
@Aspect
class BlackListHandle {

    companion object {
        private const val FILE_NAME = "black_list.json"
    }

    val blackListData: BlackListData by lazy {
        ReadWriteFile.readCurrentFileJson(FILE_NAME).to<BlackListData>()
    }

    init {
        if (!ReadWriteFile.fileExists(FILE_NAME)) {
            val stream = ReadWriteFile.createCurrentFile(FILE_NAME)
            stream.use {
                val data = BlackListData()
                it.write(data.toJSONByteArray())
            }
        }
    }

    /**
     * 屏蔽特定用户消息
     */
    @Around("@annotation(cn.luorenmu.common.annotation.BlackList)")
    fun handle(around: ProceedingJoinPoint) {
        val args = around.args
        for (any in args) {
            if (any is MessageEvent) {
                when (any) {
                    is GroupMessageEvent -> {
                        if (!statusHandle(any.userId,any.groupId)){
                            return
                        }
                    }

                    is PrivateMessageEvent -> {
                        if (!statusHandle(any.userId)){
                            return
                        }
                    }
                }

            }
        }
        around.proceed()
    }

    private fun statusHandle(userId: Long, groupId: Long? = null): Boolean {
        if (blackListData.groupIsWhite){
            if (blackListData.groupList.contains(groupId)) {
                return true
            }
        }else{
            if (!blackListData.groupList.contains(groupId)) {
                return true
            }
        }

        if (blackListData.userIsWhite){
            if (blackListData.userList.contains(userId)) {
                return true
            }
        }else{
            if (!blackListData.userList.contains(userId)) {
                return true
            }
        }
        return false
    }

}