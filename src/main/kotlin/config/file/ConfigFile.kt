package cn.luorenmu.config.file

import cn.luorenmu.common.utils.PathUtils
import cn.luorenmu.file.ReadWriteFile
import com.alibaba.fastjson2.to
import java.io.File

/**
 *
 * @author LoMu
 * Date 2025/8/24 09:41
 */
object ConfigFile {

    inline fun <reified T> updateFile(name: String, t: T) {
        val path = PathUtils.getConfigPath(name)
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
        ReadWriteFile.entityWriteFile<T>(path, t)
    }

    inline fun <reified T> loadFile(name: String, t: T): T {
        val path = PathUtils.getConfigPath(name)
        if (!File(path).exists()) {
            ReadWriteFile.entityWriteFile<T>(path, t)
            return t
        } else {
            return ReadWriteFile.readFileJson(path).to<T>()
        }
    }
}