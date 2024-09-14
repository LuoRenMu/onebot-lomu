package cn.luorenmu.repository.entiy

import com.mikuac.shiro.dto.event.message.GroupMessageEvent.GroupSender
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * @author LoMu
 * Date 2024.09.01 14:47
 */
@Document(collection = "eternal_return_push")
data class EternalReturnPush(
    @Id
    var id: String?,
    @Indexed
    var email: String,
    var sender: GroupSender,
    var createdDate: LocalDateTime,
    var subscribeGroupId: Long,
    var send: Boolean,
)