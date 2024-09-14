package cn.luorenmu.config.shiro.customAction


import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.action.common.ActionData

/**
 * @author LoMu
 * Date 2024.09.10 04:51
 */
fun Bot.setMsgEmojiLike(msgid: String, face: String): ActionData<*> {
    val action = SetEmojiLikeActionPath.SetMsgEmojiLike
    // 构建请求参数
    val map = hashMapOf<String, Any>("emoji_id" to face, "message_id" to msgid)

    // 参考 Bot 类对响应结果进行处理
    return this.customRequest(action, map)

}