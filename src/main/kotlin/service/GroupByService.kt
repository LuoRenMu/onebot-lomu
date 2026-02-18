package cn.luorenmu.service

import cn.luorenmu.common.file.ConfigFile
import org.springframework.stereotype.Service

/**
 *
 * @author LoMu
 * Date 2026/2/3 10:28
 */
@Service
class GroupByService {
    companion object {
        const val GROUP_FILE_NAME = "group"
    }

    private val group = ConfigFile.loadFile(GROUP_FILE_NAME, mutableMapOf<String, MutableSet<Long>>())

    fun add(id: Long, name: String) {
        group[name]?.add(id) ?: run {
            group[name] = mutableSetOf(id)
        }
        ConfigFile.updateFile(GROUP_FILE_NAME, group)
    }

    fun addAll(ids: List<Long>, name: String) {
        group[name]?.addAll(ids) ?: run {
            group[name] = ids.toMutableSet()
        }
        ConfigFile.updateFile(GROUP_FILE_NAME, group)
    }

    fun get(name: String): List<Long>? {
        return group[name]?.toList()
    }
}