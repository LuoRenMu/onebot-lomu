import cn.luorenmu.MainApplication
import cn.luorenmu.action.draw.EternalReturnCutoffsDraw
import cn.luorenmu.action.render.EternalReturnFindPlayerRender
import cn.luorenmu.exception.LoMuBotException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

/**
 *
 * @author LoMu
 * Date 2025/9/10 15:47
 */
@SpringBootConfiguration
@SpringBootTest(classes = [MainApplication::class], webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class RequestTest(
    @Autowired val eternalReturnFindPlayerRender: EternalReturnFindPlayerRender,
    @Autowired val eternalReturnCutoffsDraw: EternalReturnCutoffsDraw,
) {
    private val log = KotlinLogging.logger {}

    @Test
    fun testPlayer() {
        runBlocking {
            val path = eternalReturnFindPlayerRender.imageRenderGenerate("神圣审判")
            log.info { "testPlayer -> $path" }
            if (!File(path).exists()) {
                throw LoMuBotException("imageRenderGenerate错误")
            }
        }
    }

    @Test
    fun testCutoffs() {
        val path = eternalReturnCutoffsDraw.cutoffs()
        log.info { "testCutoffs -> $path" }
        if (!File(path).exists()) {
            throw LoMuBotException("cutoffs错误")
        }
    }
}