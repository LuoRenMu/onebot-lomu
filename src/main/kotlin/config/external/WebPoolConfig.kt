package cn.luorenmu.config.external

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * @author LoMu
 * Date 2024.09.14 12:51
 */
@Component
@ConfigurationProperties("web")
data class WebPoolConfig(
    var poolSize: Int = 4,
)