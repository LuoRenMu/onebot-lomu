import action.commandProcess.eternalReturn.entity.tier.EternalReturnTierDistributions
import cn.luorenmu.common.utils.HTTPRequestUtil
import cn.luorenmu.common.utils.ReadWriteFile

/**
 *
 * @author LoMu
 * Date 2025/9/10 15:47
 */
class RequestTest

suspend fun main() {
    println(ReadWriteFile.CURRENT_PATH)
    val req =
        HTTPRequestUtil.RequestEntity("https://er.dakgg.io/api/v0/statistics/tier-distribution?teamMode=SQUAD&hl=zh_CN")
    val resp = HTTPRequestUtil.callDTO<EternalReturnTierDistributions>(req)
    println(resp)
}