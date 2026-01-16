package cn.luorenmu.action.request.entity

/**
 *
 * @author LoMu
 * Date 2025/10/10 17:04
 */
data class EternalReturnRequestError(
    val error: EternalReturnRequestErrorEntity,
) {
    data class EternalReturnRequestErrorEntity(
        val status: Int,
        val message: String,
    )
}