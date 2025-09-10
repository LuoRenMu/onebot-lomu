package cn.luorenmu.config

import cn.luorenmu.action.petpet.TemplateRegister
import cn.luorenmu.common.utils.PathUtils
import cn.luorenmu.common.utils.ReadWriteFile
import cn.luorenmu.common.utils.WebPool
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

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
    private val resourceLoader: ResourceLoader,
) {
    init {
        val initFiles =
            mapOf(
                "static/images/bg-character.jpg" to PathUtils.getEternalReturnDataImagePath("bg-nickname.jpg"),
                "static/character.txt" to ReadWriteFile.currentPathFileName("character.txt"),
                "static/player.txt" to ReadWriteFile.currentPathFileName("player.txt")
            )

        // 生成必要文件
        for (file in initFiles) {
            resourceLoader.getResource("classpath:" + file.key).inputStream.buffered().use {
                ReadWriteFile.writeStreamFile(file.value, it)
            }
        }
        log.info { "PetPet模板已加载 -> ${TemplateRegister.petPetTemplates.map { it.key }}" }


    }

    @Bean(destroyMethod = "shutdown")
    fun getWebPageScreenshotPool(): WebPool {
        return WebPool(pool, headless)
    }


}

