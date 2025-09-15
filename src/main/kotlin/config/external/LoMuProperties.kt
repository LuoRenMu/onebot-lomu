package cn.luorenmu.config.external

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 *
 * @author LoMu
 * Date 2025/9/12 14:29
 */

@ConfigurationProperties("lomu")
data class LoMuProperties(
    @NestedConfigurationProperty
    val web: WebPoolConfig,
    @NestedConfigurationProperty
    val eter: EternalReturnConfig,
    @NestedConfigurationProperty
    val bot: BotConfig,
)
