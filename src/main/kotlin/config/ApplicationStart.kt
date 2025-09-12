package cn.luorenmu.config

import cn.luorenmu.action.petpet.TemplateRegister
import cn.luorenmu.common.utils.PathUtils
import cn.luorenmu.common.utils.ReadWriteFile
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.File

/**
 *
 * @author LoMu
 * Date 2025/9/11 19:18
 */
@Configuration
class ApplicationStart : ApplicationListener<ApplicationStartedEvent> {

    private val log = KotlinLogging.logger {}
    override fun onApplicationEvent(event: ApplicationStartedEvent) {
        val initFiles = mapOf(
            "static/images/bg-character.jpg" to PathUtils.getEternalReturnDataImagePath("bg-character.jpg"),
        )

        // 生成必要文件
        for (file in initFiles) {
            if (!File(file.value).exists()) {
                ClassPathResource(file.key).inputStream.buffered().use {
                    ReadWriteFile.writeStreamFile(file.value, it)
                }
            }
        }
        log.info { "PetPet模板已加载 -> ${TemplateRegister.petPetTemplates.map { it.key }}" }

    }
}