package cn.luorenmu.config

import cn.luorenmu.action.petpet.TemplateRegister
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Configuration

/**
 *
 * @author LoMu
 * Date 2025/9/11 19:18
 */
@Configuration
class ApplicationStart : ApplicationListener<ApplicationStartedEvent> {

    private val log = KotlinLogging.logger {}
    override fun onApplicationEvent(event: ApplicationStartedEvent) {
        log.info { "PetPet模板已加载 -> ${TemplateRegister.petPetTemplates.map { it.key }}" }
    }
}