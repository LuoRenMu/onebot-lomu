package cn.luorenmu.common.utils

import java.io.File

/**
 * @author LoMu
 * Date 2024.08.03 9:34
 */
object PathUtils {
    private fun createLastDir(path: String) {
        val lastIndexOf = path.lastIndexOf("/")
        File(path.substring(0, lastIndexOf)).mkdirs()
    }

    fun getConfigPath(fileName: String): String {
        val path = ReadWriteFile.currentPathFileName("config/$fileName")
        createLastDir(path)
        return path
    }

    fun getVideoPath(fileName: String): String {
        val path = ReadWriteFile.currentPathFileName("video/$fileName")
        createLastDir(path)
        return path
    }

    fun getImagePath(name: String): String {
        val path = ReadWriteFile.currentPathFileName("image/${name}")
        createLastDir(path)
        return path
    }

    fun getRenderPath(name: String): String {
        val path = ReadWriteFile.currentPathFileName("render/${name}")
        createLastDir(path)
        return path
    }
}
