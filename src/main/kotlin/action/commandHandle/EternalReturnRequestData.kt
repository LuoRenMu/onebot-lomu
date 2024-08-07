package cn.luorenmu.action.commandHandle

import cn.luorenmu.action.commandHandle.entiy.eternalReturn.*
import cn.luorenmu.common.utils.dakggCdnUrl
import cn.luorenmu.common.utils.getEternalReturnDataImagePath
import cn.luorenmu.entiy.Request.RequestDetailed
import cn.luorenmu.file.ReadWriteFile
import cn.luorenmu.request.RequestController
import com.alibaba.fastjson2.to
import com.alibaba.fastjson2.toJSONString
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.io.File
import java.net.SocketException
import java.util.concurrent.TimeUnit

/**
 * @author LoMu
 * Date 2024.08.03 9:11
 */
@Component
class EternalReturnRequestData(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    fun findExistPlayers(nickname: String): Boolean {
        val requestController = RequestController("eternal_return_request.find_player")
        requestController.replaceUrl("nickname", nickname)
        try {
            var request = requestController.request()
            var body = request.body()
            if (body.contains("retry_after")) {
                request = requestController.request()
                body = request.body()
            }
            return !body.contains("not_found")
        } catch (e: SocketException) {
            return true
        }
    }

    fun tierDistributionsFind(): EternalReturnTierDistributions? {
        val request = RequestController("eternal_return_request.tier_distribution")
        val resp = request.request()
        if (resp.isOk) {
            return resp.body().to<EternalReturnTierDistributions>()
        }
        return null
    }

    fun leaderboardFind(): EternalRetrunLeaderboard? {
        currentSeason()?.let {
            val requestLeaderboard = RequestController("eternal_return_request.leaderboard")
            requestLeaderboard.replaceUrl("season", it.type)
            val respLeaderboard = requestLeaderboard.request()
            if (respLeaderboard.isOk) {
                val leaderboard = respLeaderboard.body().to<EternalRetrunLeaderboard>()
                leaderboard.currentSeason = it
                return leaderboard
            }
        }
        return null
    }

    fun checkCharacterImgExistThenGetPathOrDownload(name: String): String {
        val eternalReturnDataImagePath = getEternalReturnDataImagePath("character/${name}.png")
        val fileExists = File(eternalReturnDataImagePath).exists()
        if (!fileExists) {
            characterFind().let {
                val character = it?.characters?.stream()?.filter { c -> c.key == name }?.findFirst()
                val communityImageUrl = character!!.get().communityImageUrl
                val dakGGCdnUrl = dakggCdnUrl(communityImageUrl)
                val requestDetailed = RequestDetailed()
                requestDetailed.url = dakGGCdnUrl
                requestDetailed.method = "get"
                val request = RequestController(requestDetailed)
                val resp = request.request()
                if (resp.isOk) {
                    ReadWriteFile.writeStreamFile(eternalReturnDataImagePath, resp.bodyStream())
                }
            }
        }
        return getEternalReturnDataImagePath("character/${name}.png")
    }

    fun characterLeaderboardFind(character: String, sortType: String): EternalReturnLeaderboardCharacters? {
        currentSeason()?.let {
            val leaderboardCharacters = RequestController("eternal_return_request.leaderboard_characters")
            leaderboardCharacters.replaceUrl("season", it.type)
            leaderboardCharacters.replaceUrl("character", character)
            leaderboardCharacters.replaceUrl("sortType", sortType)
            val resp = leaderboardCharacters.request()
            if (resp.isOk) {
                return resp.body().to<EternalReturnLeaderboardCharacters>()
            }
        }
        return null
    }

    fun characterFind(): EternalReturnCharacter? {
        redisTemplate.opsForValue().get("Eternal_Return_Find: characters")?.let {
            return it.to<EternalReturnCharacter>()
        }
        val requestController = RequestController("eternal_return_request.character")
        val resp = requestController.request()
        if (resp.isOk) {
            val characters = resp.body().to<EternalReturnCharacter>()
            redisTemplate.opsForValue()["Eternal_Return_Find: characters", characters.toJSONString(), 7L] =
                TimeUnit.DAYS
            return characters
        }
        return null
    }

    fun currentSeason(): EternalCurrentSeason? {
        val requestCurrentSeason = RequestController("eternal_return_request.current_season")
        val respCurrentSeason = requestCurrentSeason.request()
        if (respCurrentSeason.isOk) {
            return respCurrentSeason.body().to<EternalCurrentSeason>()
        }
        return null
    }
}