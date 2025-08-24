package cn.luorenmu.config

import cn.hutool.core.io.resource.ResourceUtil
import cn.luorenmu.MainApplication
import cn.luorenmu.action.petpet.TemplateRegister
import cn.luorenmu.common.utils.PathUtils
import cn.luorenmu.common.utils.WebPool
import cn.luorenmu.config.entity.CharacterNickNameList
import cn.luorenmu.config.file.BlackListManager
import cn.luorenmu.config.file.EternalReturnCharacterAliasName
import cn.luorenmu.config.file.PermissionsManager
import cn.luorenmu.file.InitializeFile
import cn.luorenmu.file.ReadWriteFile
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author LoMu
 * Date 2024.07.25 2:16
 */

private val log = KotlinLogging.logger {}

@Configuration
class BootStrapConfig {
    init {


        InitializeFile.run(MainApplication::class.java)
        val initFiles =
            mapOf(
                "static/images/bg-character.jpg" to PathUtils.getEternalReturnDataImagePath("bg-character.jpg"),
                "static/images/deer.jpg" to PathUtils.getImagePath("deer.jpg"),
                "static/images/deerTemplate.jpg" to PathUtils.getImagePath("deerTemplate.jpg"),
                "static/images/fuckdeer.jpg" to PathUtils.getImagePath("fuckdeer.jpg"),
            )

        // 生成必要文件
        for (file in initFiles) {
            ResourceUtil.getResource(file.key).openStream().buffered().use {
                ReadWriteFile.writeStreamFile(file.value, it)
            }
        }
        log.info { "黑名单列表已加载 -> ${BlackListManager.blackListData}" }
        log.info { "权限列表已加载 -> ${PermissionsManager.permissionData}" }
        log.info { "PetPet模板已加载 -> ${TemplateRegister.petPetTemplates.map { it.key }}" }

    }

    @Bean(destroyMethod = "shutdown")
    fun getWebPageScreenshotPool(): WebPool {
        return WebPool(5, true)
    }

    @Bean
    fun getCharacterNickName(): CharacterNickNameList {
        return EternalReturnCharacterAliasName.getCharacterNickName()
    }

}

