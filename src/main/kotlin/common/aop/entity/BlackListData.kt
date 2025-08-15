package cn.luorenmu.common.aop.entity

data class BlackListData(
    val groupList: MutableList<Long> = mutableListOf(),
    val userList: MutableList<Long> = mutableListOf(),
    var groupIsWhite: Boolean = false,
    var userIsWhite: Boolean = false
)