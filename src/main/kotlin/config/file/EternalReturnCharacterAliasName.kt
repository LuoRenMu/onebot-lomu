package cn.luorenmu.config.file

import cn.luorenmu.config.entity.CharacterNickName
import cn.luorenmu.config.entity.CharacterNickNameList
import cn.luorenmu.file.ReadWriteFile
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 * @author LoMu
 * Date 2025/8/24 03:53
 */
object EternalReturnCharacterAliasName {
    fun getCharacterNickName(): CharacterNickNameList {
        val characterNickNames = CopyOnWriteArrayList<CharacterNickName>()
        val lines: List<String> = File(ReadWriteFile.currentPathFileName("character.txt.txt")).readLines()
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