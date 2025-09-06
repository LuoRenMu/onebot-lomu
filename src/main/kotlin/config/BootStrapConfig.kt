package cn.luorenmu.config

import cn.hutool.core.io.resource.ResourceUtil
import cn.luorenmu.MainApplication
import cn.luorenmu.action.petpet.TemplateRegister
import cn.luorenmu.common.utils.PathUtils
import cn.luorenmu.common.utils.WebPool
import cn.luorenmu.config.entity.CharacterNickNameList
import cn.luorenmu.config.file.EternalReturnCharacterAliasName
import cn.luorenmu.file.InitializeFile
import cn.luorenmu.file.ReadWriteFile
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author LoMu
 * Date 2024.07.25 2:16
 */

private val log = KotlinLogging.logger {}

@Configuration
class BootStrapConfig(
    @Value("\${web.pool:3}")
    private val pool: Int,
    @Value("\${web.headless:true}")
    private val headless: Boolean,
) {
    init {
        InitializeFile.run(MainApplication::class.java)
        val initFiles =
            mapOf(
                "static/images/bg-character.jpg" to PathUtils.getEternalReturnDataImagePath("bg-character.txt.jpg"),
                "static/character.txt.txt" to ReadWriteFile.currentPathFileName("character.txt.txt")
            )

        // 生成必要文件
        for (file in initFiles) {
            ResourceUtil.getResource(file.key).openStream().buffered().use {
                ReadWriteFile.writeStreamFile(file.value, it)
            }
        }
        log.info { "PetPet模板已加载 -> ${TemplateRegister.petPetTemplates.map { it.key }}" }


    }

    @Bean(destroyMethod = "shutdown")
    fun getWebPageScreenshotPool(): WebPool {
        return WebPool(pool, true)
    }

    @Bean
    fun getCharacterNickName(): CharacterNickNameList {
        return EternalReturnCharacterAliasName.getCharacterNickName()
    }

}

