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


fun String.replaceAtToEmpty(id: Long): String {
    return this.replace(MsgUtils.builder().at(id).build(), "")
}

fun String.replaceAtToEmpty(): String {
    return this.lowercase().replace("\\[CQ:at,qq=(\\d+)]".lowercase().toRegex(), "")
}

fun String.getAtQQ(i: Int = 0): String? {
    return "\\[CQ:at,qq=(\\d+)?+]".lowercase().toRegex().findAll(this.lowercase()).toMutableList()
        .getOrNull(i)?.groupValues?.get(1)
}

fun String.replaceBlankToEmpty(): String {
    return this.replace(" ", "")
}

fun String.replaceReplyToEmpty(): String {
    return this.lowercase().replace("\\[CQ:reply,id=(\\d+)]".lowercase().toRegex(), "")
}

fun String.replaceImageToEmpty(): String {
    return this.lowercase().replace("\\[CQ:image,.*?]".lowercase().toRegex(), "")
}

fun String.toPinYin(): String {
    val string = StringBuilder()
    for (i in this.indices) {
        string.append(Pinyin.toPinyin(this[i]))
    }
    return string.toString()
}


fun String.getCQReplyMessageId(): String? {
    if (this.isCQReply()) {
        return "\\[CQ:reply,id=(\\d+)?+]".toRegex().find(this)?.groups?.get(1)?.value
    }
    return null
}


fun String.getCQFileStr(index: Int = 0): String? {
    val regex = """file=([^,]+)""".toRegex()
    val matchResult = regex.findAll(this).toList()
    if (matchResult.isNotEmpty()) {
        return matchResult.getOrNull(index)?.groupValues?.get(1)
    }
    return null
}

fun String.getFileStr(index: Int = 0): String? {
    val regex = """"file"\s*:\s*"([^"]+)"""".toRegex()
    val matchResult = regex.findAll(this).toList()
    if (matchResult.isNotEmpty()) {
        return matchResult.getOrNull(index)?.groupValues?.get(1)
    }
    return null
}


fun String.isCQReply(): Boolean {
    return this.lowercase().contains("\\[CQ:reply,id=\\d+]".toRegex())
}


