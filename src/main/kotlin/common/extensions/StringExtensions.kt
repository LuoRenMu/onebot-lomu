package cn.luorenmu.common.extensions

import com.github.promeg.pinyinhelper.Pinyin
import com.mikuac.shiro.common.utils.MsgUtils

/**
 * @author LoMu
 * Date 2024.09.05 14:12
 */
fun String.firstPinYin(): String {
    val string = StringBuilder()
    for (i in this.indices) {
        string.append(Pinyin.toPinyin(this[i]).first())
    }
    return string.toString()
}

fun String.toPinYin(): String {
    val string = StringBuilder()
    for (i in this.indices) {
        string.append(Pinyin.toPinyin(this[i]))
    }
    return string.toString()
}

fun String.isImage(): Boolean {
    return this.startsWith("[CQ:image") && this.endsWith("]")
}


// QQ表情包
fun String.isMface(): Boolean {
    return this.startsWith("[CQ:mface") && this.endsWith("]")
}

fun String.getCQFileStr(): String? {
    if (this.startsWith("[CQ:image") && this.endsWith("]")) {
        val regex = """file=([^,]+)""".toRegex()
        val matchResult = regex.find(this)
        if (matchResult != null) {
            return matchResult.groupValues[1]
        }
    }
    return null
}

fun String.replaceCqToFileStr(): String? {
    if (this.isImage()) {
        return this.getCQFileStr()
    }
    return null
}

fun String.isCQAt(): Boolean {
    return this.contains("[CQ:at")
}

fun String.isAt(id: Long): Boolean {
    return this.contains(MsgUtils.builder().at(id).build())
}

fun String.isCQReply(): Boolean {
    return this.contains("[CQ:reply")
}


fun String.isCQStr(): Boolean {
    return this.contains("[CQ:")
}