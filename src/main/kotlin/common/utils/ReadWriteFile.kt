package cn.luorenmu.common.utils

import cn.luorenmu.MainApplication
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONWriter
import java.io.*
import java.nio.charset.StandardCharsets

/**
 * @author LoMu
 * Date 2023.11.22 23:03
 */
object ReadWriteFile {
    val CURRENT_PATH: String by lazy { scanFilePath() }


    /**
     * 获取jar文件所在目录
     */
    private fun scanFilePath(): String {
        val location = MainApplication::class.java.protectionDomain.codeSource.location
        val uri = location.toURI()
        var file = File(uri)
        if (file.isFile && file.name.lowercase().endsWith(".jar")) {
            file = file.parentFile
        }
        return file.absolutePath
    }


    fun readFileJson(path: String): String {
        val sb = StringBuilder()
        try {
            BufferedReader(FileReader(path)).use { bufferedReader ->
                var s: String?
                while ((bufferedReader.readLine().also { s = it }) != null) {
                    sb.append(s).append(System.lineSeparator())
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return sb.toString()
    }


    fun writeStreamFile(fileName: String, inputStream: InputStream): File {
        val file = File(fileName)
        if (file.exists()) {
            return file
        }
        val currentFile = createFile(fileName)
        try {
            BufferedOutputStream(currentFile).use { bufferedOutputStream ->
                val bytes = ByteArray(1024)
                var len: Int
                while ((inputStream.read(bytes).also { len = it }) != -1) {
                    bufferedOutputStream.write(bytes, 0, len)
                    bufferedOutputStream.flush()
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return file
    }


    fun createCurrentDirs(fileName: String) {
        var fileName = fileName
        fileName = currentPathFileName(fileName)
        if (fileName.contains("/") || fileName.contains("\\")) {
            fileName = fileName.replace("\\\\".toRegex(), "/")
        }
        val indexOf = fileName.lastIndexOf("/")
        val substring = fileName.substring(0, indexOf)
        val file = File(substring)
        if (!file.exists()) {
            file.mkdirs()
        }
    }

    fun createFile(path: String): OutputStream {
        val file = File(path)
        if (!file.exists()) {
            try {
                var i = path.lastIndexOf("/")
                if (i == -1) {
                    i = path.lastIndexOf("\\")
                }
                val dirStr = path.substring(0, i)
                val dir = File(dirStr)
                dir.mkdirs()
                file.createNewFile()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        try {
            return FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        }
    }


    fun currentPathFileName(fileName: String): String {
        return CURRENT_PATH + fileName
    }


    fun <T> entityWriteFile(path: String, t: T?) {
        try {
            createFile(path).use { outputStream ->
                val jsonString = JSON.toJSONString(t, JSONWriter.Feature.PrettyFormat)
                outputStream.write(jsonString.toByteArray(StandardCharsets.UTF_8))
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }


    fun currentDirs(fileName: String): Array<File> {
        val file = File(currentPathFileName(fileName))
        return file.listFiles()
    }
}