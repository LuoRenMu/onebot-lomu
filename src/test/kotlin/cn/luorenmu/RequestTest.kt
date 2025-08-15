package cn.luorenmu

import cn.luorenmu.common.aop.entity.BlackListData
import cn.luorenmu.file.InitializeFile
import cn.luorenmu.file.ReadWriteFile
import com.alibaba.fastjson2.toJSONByteArray


/**
 * @author LoMu
 * Date 2025.02.20 14:13
 */

private const val FILE_NAME = "black_list.json"


fun main() {
    InitializeFile.run(MainApplication::class.java)
    println(ReadWriteFile.CURRENT_PATH)
    if (!ReadWriteFile.fileExists(FILE_NAME)) {
        val stream = ReadWriteFile.createCurrentFile(FILE_NAME)
        stream.use {
            val data = BlackListData()
            it.write(data.toJSONByteArray())
        }
    }
}