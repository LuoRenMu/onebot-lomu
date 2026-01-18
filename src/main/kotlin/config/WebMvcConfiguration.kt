package cn.luorenmu.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


/**
 *
 * @author LoMu
 * Date 2026/1/19 03:34
 */

@Configuration
class WebMvcConfiguration : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/images/**")
            .addResourceLocations("classpath:/static/images/")
    }
}
