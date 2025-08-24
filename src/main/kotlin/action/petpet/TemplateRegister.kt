package cn.luorenmu.action.petpet

import cn.luorenmu.file.ReadWriteFile
import io.github.oshai.kotlinlogging.KotlinLogging
import moe.dituon.petpet.old_template.OldPetpetTemplate
import moe.dituon.petpet.template.PetpetTemplate
import java.io.File

/**
 * @author LoMu
 * Date 2025.02.04 20:39
 */
object TemplateRegister {
    private val log = KotlinLogging.logger {}
    val petPetTemplates: HashMap<String, PetpetTemplate> by lazy { register() }
    fun getTemplate(id: String): PetpetTemplate? {
        return petPetTemplates[id] ?: run { return null }
    }

    fun register(): HashMap<String, PetpetTemplate> {
        if (!File(ReadWriteFile.currentPathFileName("templates")).exists()) {
            log.info { "PetPet模板 没有找到目录templates" }
            return hashMapOf()
        }
        val jsonAll = readDirsAllJson(ReadWriteFile.currentDirs("templates"))
        val petpetTemplates = hashMapOf<String, PetpetTemplate>()
        jsonAll.forEach {
            val petpetTemplate = if (it.name.contains("template")) {
                PetpetTemplate.fromJsonFile(it)
            } else {
                OldPetpetTemplate.fromJsonFile(it).toTemplate()
            }

            for (petpetName in petpetTemplate.metadata.alias) {
                petpetTemplates[petpetName] = petpetTemplate
            }
        }
        return petpetTemplates
    }

    private fun readDirsAllJson(
        files: Array<File>,
        jsonFiles: MutableList<File> = mutableListOf(),
    ): MutableList<File> {
        for (file in files) {
            if (file.isDirectory) {
                readDirsAllJson(File(file.path).listFiles()!!, jsonFiles)
            }
            if (file.name.endsWith("data.json")) {
                jsonFiles.add(file)
            }
            if (file.name.endsWith("template.json")) {
                jsonFiles.add(file)
            }
        }
        return jsonFiles
    }

}

