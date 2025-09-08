package cn.luorenmu.config.entity

/**
 * @author LoMu
 * Date 2025.05.30 00:12
 */
data class AliasName(
    val nickname: String,
    val alias: MutableList<String> = mutableListOf(),
)