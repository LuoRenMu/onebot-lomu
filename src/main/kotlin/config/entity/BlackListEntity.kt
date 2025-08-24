package cn.luorenmu.config.entity

data class BlackListEntity(
    val groupList: MutableList<Long> = mutableListOf(),
    val userList: MutableList<Long> = mutableListOf(),
    var groupIsWhite: Boolean = false,
    var userIsWhite: Boolean = false,
)