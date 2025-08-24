package cn.luorenmu.config.file

import cn.hutool.core.codec.Base64Decoder
import cn.hutool.core.io.resource.ResourceUtil
import cn.luorenmu.config.entity.CharacterNickName
import cn.luorenmu.config.entity.CharacterNickNameList
import cn.luorenmu.entiy.Request
import cn.luorenmu.request.RequestController
import com.alibaba.fastjson2.JSON
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 * @author LoMu
 * Date 2025/8/24 03:53
 */
object EternalReturnCharacterAliasName {
    private val log = KotlinLogging.logger { }
    fun getCharacterNickName(): CharacterNickNameList {
        val characterNickNames = CopyOnWriteArrayList<CharacterNickName>()
        var lines: List<String>

        try {
            val requestController = RequestController(Request.RequestDetailed().apply {
                url =
                    "https://api.github.com/repos/LoMuBot/EternalReturn-Alias/contents/character.txt?ref=main"
                method = "get"
            })
            val resp = requestController.request()
            val json = JSON.parseObject(resp.body())
            val decode = Base64Decoder.decode(json["content"].toString())
            val str = String(decode)
            lines = str.lines()
        } catch (_: Exception) {
            log.error { "远程读取实验体别名失败,读取本地数据" }
            lines = ResourceUtil
                .getResource("character").openStream().bufferedReader().readText().lines()
        }

        lines.forEach {
            if (it.isNotBlank()) {
                val list = it.split(":").toMutableList()
                list.removeIf { l -> l.isBlank() }
                val first = list.removeFirst()
                characterNickNames.add(CharacterNickName(first, list))
            }
        }
        return CharacterNickNameList(characterNickNames)
    }


}