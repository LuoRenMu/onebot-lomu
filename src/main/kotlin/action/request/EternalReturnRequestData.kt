package cn.luorenmu.action.request

import action.commandProcess.eternalReturn.entity.EternalRetrunLeaderboard
import action.commandProcess.eternalReturn.entity.EternalReturnCharacter
import action.commandProcess.eternalReturn.entity.EternalReturnCharacterInfo
import action.commandProcess.eternalReturn.entity.EternalReturnCurrentSeason
import action.commandProcess.eternalReturn.entity.profile.EternalReturnProfile
import cn.luorenmu.action.commandHandle.entiy.eternalReturn.EternalReturnTierDistributions
import cn.luorenmu.common.utils.PathUtils
import cn.luorenmu.entiy.Request.RequestDetailed
import cn.luorenmu.file.ReadWriteFile
import cn.luorenmu.request.RequestController
import com.alibaba.fastjson2.JSONException
import com.alibaba.fastjson2.to
import io.github.oshai.kotlinlogging.KotlinLogging
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
    private val requestData: RequestData,
) {
    private val log = KotlinLogging.logger {}

    // sync player
    fun syncPlayers(nickname: String, counter: Int = 0): Boolean {
        if (counter == 5) {
            return true
        }
        val requestController = RequestController("eternal_return_request.find_player")
        requestController.replaceUrl("nickname", nickname)
        try {
            val request = requestController.request()
            request?.let {
                val body = request.body()
                if (body.contains("retry_after")) {
                    return syncPlayers(nickname, counter + 1)
                }
                if (body.contains("invalid name")) {
                    return false
                }
                return !body.contains("not_found")
            }
        } catch (e: SocketException) {
            return true
        } catch (e: Exception) {
            return syncPlayers(nickname, counter + 1)
        }
        return true
    }


    fun tierDistributionsFind(): EternalReturnTierDistributions? {
        val request = RequestController("eternal_return_request.tier_distribution")
        val resp = request.request()
        return resp?.body().to<EternalReturnTierDistributions>()

    }

    fun leaderboardFind(): EternalRetrunLeaderboard? {
        return currentSeason()?.let {
            val requestLeaderboard = RequestController("eternal_return_request.leaderboard")
            requestLeaderboard.replaceUrl("season", it.currentSeason.key)
            val respLeaderboard = requestLeaderboard.request()
            respLeaderboard?.let { resp ->
                val leaderboard = resp.body().to<EternalRetrunLeaderboard>()
                leaderboard.currentSeason = it
                leaderboard
            }
        }
    }

    private fun dakGGDownloadStreamFile(streamUrl: String, outputPath: String) {
        val dakGGCdnUrl = PathUtils.dakggCdnUrl(streamUrl)
        val requestDetailed = RequestDetailed()
        requestDetailed.url = dakGGCdnUrl
        requestDetailed.method = "get"
        val request = RequestController(requestDetailed)
        val resp = request.request()
        resp?.let { ReadWriteFile.writeStreamFile(outputPath, resp.bodyStream()) }
    }

    fun checkTierIconExistThenGetPathOrDownload(id: Int): String {
        val eternalReturnDataImagePath = PathUtils.getEternalReturnDataImagePath("tier/${id}.png")
        if (!File(eternalReturnDataImagePath).exists()) {
            dakGGDownloadStreamFile("/er/images/tier/round/$id.png", eternalReturnDataImagePath)
        }
        return eternalReturnDataImagePath
    }

    fun checkCharacterImgExistThenGetPathOrDownload(name: String): String {
        val eternalReturnDataImagePath = PathUtils.getEternalReturnDataImagePath("character/${name}.png")
        val fileExists = File(eternalReturnDataImagePath).exists()
        if (!fileExists) {
            characterFind().let {
                val character = it?.characters?.stream()?.filter { c -> c.key == name }?.findFirst()
                val communityImageUrl = character!!.get().communityImageUrl
                dakGGDownloadStreamFile(communityImageUrl, eternalReturnDataImagePath)
            }
        }
        return PathUtils.getEternalReturnDataImagePath("character/${name}.png")
    }


    fun characterInfoFind(character: String, weapon: String, token: String): EternalReturnCharacterInfo? {
        redisTemplate.opsForValue().get("Eternal_Return_Find:${character}")?.to<EternalReturnCharacterInfo>()
        val request = RequestController("eternal_return_request.find_character_info")
        request.replaceUrl("token", token)
        request.replaceUrl("key", character)
        request.replaceUrl("key1", character)
        request.replaceUrl("weapon", weapon)
        val resp = request.request()
        if ((resp?.status ?: 500) != 200) {
            return null
        }
        try {
            val result = resp.body().to<EternalReturnCharacterInfo>()
            redisTemplate.opsForValue()["Eternal_Return_Find:${character}", resp.body(), 2L] =
                TimeUnit.DAYS
            return result
        } catch (e: JSONException) {
            return null
        }
    }


    fun characterFind(): EternalReturnCharacter? {
        redisTemplate.opsForValue().get("Eternal_Return_Find: characters")?.let {
            return it.to<EternalReturnCharacter>()
        }
        val requestController = RequestController("eternal_return_request.character")
        val resp = requestController.request()
        return resp?.let {
            val characters = it.body().to<EternalReturnCharacter>()
            redisTemplate.opsForValue()["Eternal_Return_Find: characters", it.body(), 1L] =
                TimeUnit.DAYS
            characters
        }

    }

    fun currentSeason(): EternalReturnCurrentSeason? {
        val requestCurrentSeason = RequestController("eternal_return_request.current_season")
        val respCurrentSeason = requestCurrentSeason.request()
        return respCurrentSeason?.body().to<EternalReturnCurrentSeason>()
    }

    fun checkPlayerExists(name: String): Boolean {
        val requestProfile = RequestController("eternal_return_request.profile")
        requestProfile.replaceUrl("season", "SEASON_1")
        requestProfile.replaceUrl("name", name)
        val requestRetry = requestData.requestRetry(requestProfile)
        requestRetry?.let {
            return true
        }
        return false
    }

    fun profile(name: String, season: String = "SEASON_1"): EternalReturnProfile? {
        val requestProfile = RequestController("eternal_return_request.profile")
        requestProfile.replaceUrl("season", season)
        requestProfile.replaceUrl("name", name)
        val resp = requestProfile.request()
        return try {
            resp?.body().to<EternalReturnProfile>()
        } catch (e: JSONException) {
            log.error { e.printStackTrace() }
            null
        }
    }

    fun news(id: String): String? {
        val requestProfile = RequestController("eternal_return_request.news")
        requestProfile.replaceUrl("id", id)
        val resp = requestProfile.request()
        if (resp.status != 200) {
            return null
        }
        return resp?.body()
    }
}