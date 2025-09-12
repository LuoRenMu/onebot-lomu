package cn.luorenmu.config.file

import cn.luorenmu.common.utils.ReadWriteFile
import cn.luorenmu.config.entity.AliasName
import cn.luorenmu.config.entity.AliasNameListEntity
import org.springframework.core.io.ClassPathResource
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 * @author LoMu
 * Date 2025/8/24 03:53
 */
object EternalReturnAliasName {
    fun getCharacterNickName(): AliasNameListEntity {
        return getNickName("character.txt")
    }

    fun getPlayerNickName(): AliasNameListEntity {
        return getNickName("player.txt")
    }


    private fun getNickName(filename: String): AliasNameListEntity {
        val path = ReadWriteFile.currentPathFileName(filename)
        val aliasNames = CopyOnWriteArrayList<AliasName>()
        val file = File(path)
        if (!file.exists()) {
            ClassPathResource("static/${filename}").inputStream.buffered().use {
                ReadWriteFile.writeStreamFile(ReadWriteFile.currentPathFileName(filename), it)
            }
        }
        val lines: List<String> = file.readLines()
        lines.forEach {
            if (it.isNotBlank()) {
                val list = it.split(":").toMutableList()
                list.removeIf { l -> l.isBlank() }
                val first = list.removeFirst()
                aliasNames.add(AliasName(first, list))
            }
        }
        return AliasNameListEntity(aliasNames)
    }


}