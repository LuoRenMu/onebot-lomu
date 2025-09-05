package cn.luorenmu.action.request

import action.commandProcess.eternalReturn.entity.EternalReturnCharacter
import action.commandProcess.eternalReturn.entity.EternalReturnCharacterById
import action.commandProcess.eternalReturn.entity.EternalReturnLeaderboard
import action.commandProcess.eternalReturn.entity.EternalReturnSeason
import action.commandProcess.eternalReturn.entity.profile.EternalReturnProfile
import action.commandProcess.eternalReturn.entity.tier.EternalReturnTierDistributions
import cn.luorenmu.action.commandProcess.eternalReturn.entity.item.EternalReturnItemInfos
import cn.luorenmu.action.commandProcess.eternalReturn.entity.matcher.EternalReturnMatches
import cn.luorenmu.action.commandProcess.eternalReturn.entity.matcher.EternalReturnMatchesById
import cn.luorenmu.action.commandProcess.eternalReturn.entity.skill.EternalReturnTacticalSkill
import cn.luorenmu.action.commandProcess.eternalReturn.entity.skill.EternalReturnTraitSkills
import cn.luorenmu.action.commandProcess.eternalReturn.entity.tier.EternalReturnTiers
import cn.luorenmu.action.commandProcess.eternalReturn.entity.weapon.EternalReturnWeapons
import cn.luorenmu.action.request.api.EternalReturnDakGGAPI
import cn.luorenmu.action.request.api.EternalReturnOfficialAPI
import cn.luorenmu.action.request.api.HTTPRequest
import cn.luorenmu.action.request.entity.EternalReturnTraitSkillImgDTO
import cn.luorenmu.common.utils.PathUtils
import cn.luorenmu.common.utils.RedisUtils
import cn.luorenmu.entity.RequestEntity.RequestDetailed
import cn.luorenmu.exception.LoMuBotException
import cn.luorenmu.request.RequestController
import com.alibaba.fastjson2.JSONException
import com.alibaba.fastjson2.to
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @author LoMu
 * Date 2024.08.03 9:11
 */
@Component
class EternalReturnRequestData(
    private val redisUtils: RedisUtils,
) {
    private val log = KotlinLogging.logger {}

    // sync player
    fun syncPlayers(nickname: String, counter: Int = 0): Boolean {
        if (counter == 3) {
            return true
        }
        val requestController = RequestController(EternalReturnDakGGAPI.Player.syncDataV0API(nickname))
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
        } catch (_: Exception) {
            return true
        }
        return true
    }

    /**
     * 段位总览(有哪些段位?)
     */
    fun tiers(): EternalReturnTiers? {
        return redisUtils.getCache("Eternal_Return: tiers", EternalReturnTiers::class.java, {
            val resp = HTTPRequest.requestRetry(RequestController(EternalReturnDakGGAPI.Data.tiersV1API()))
            resp?.body().to<EternalReturnTiers>()
        }, 2L, TimeUnit.DAYS)
    }

    /**
     * 匹配
     */
    fun matches(
        nickname: String,
        season: String,
        matchingMode: String = "ALL",
        teamMode: String = "ALL",
        page: Int = 1,
    ): EternalReturnMatches? {
        val requestController = RequestController(
            EternalReturnDakGGAPI.Player.matchesV1API(
                nickname = nickname,
                season = season,
                matchingMode = matchingMode,
                teamMode = teamMode,
                page = page,
            )
        )
        val resp = HTTPRequest.requestRetry(requestController)
        return resp?.body().to<EternalReturnMatches>()
    }

    /**
     * 段位分布
     */
    fun tierDistributionsFind(): EternalReturnTierDistributions? {
        val resp =
            HTTPRequest.requestRetry(RequestController(EternalReturnDakGGAPI.Statistics.tierDistributionV0API()))
        return resp?.body().to<EternalReturnTierDistributions>()

    }

    fun leaderboardFind(): EternalReturnLeaderboard? {
        return season()?.let {
            val requestLeaderboard =
                RequestController(EternalReturnDakGGAPI.Leaderboard.leaderboardV0API(it.currentSeason.key))
            val respLeaderboard = requestLeaderboard.request()
            respLeaderboard?.let { resp ->
                val leaderboard = resp.body().to<EternalReturnLeaderboard>()
                leaderboard.currentSeason = it
                leaderboard
            }
        }
    }


    fun characterFind(): EternalReturnCharacter? {
        return redisUtils.getCache("Eternal_Return: characters", EternalReturnCharacter::class.java, {
            val requestController = RequestController(EternalReturnDakGGAPI.Data.charactersV1API())
            val resp = HTTPRequest.requestRetry(requestController)
            resp!!.body().to<EternalReturnCharacter>()
        }, 2L, TimeUnit.DAYS)
    }

    fun season(): EternalReturnSeason? {
        return redisUtils.getCache("Eternal_Return: season", EternalReturnSeason::class.java, {
            val requestCurrentSeason = RequestController(EternalReturnDakGGAPI.Data.seasonV1API())
            val respCurrentSeason = HTTPRequest.requestRetry(requestCurrentSeason)
            respCurrentSeason?.body().to<EternalReturnSeason>()
        }, 1L, TimeUnit.DAYS)
    }


    fun profile(name: String, season: String = ""): EternalReturnProfile? {
        val requestProfile = RequestController(EternalReturnDakGGAPI.Player.profileV1API(name, season))
        val resp = HTTPRequest.requestRetry(requestProfile)
        return try {
            resp?.body().to<EternalReturnProfile>()
        } catch (e: JSONException) {
            log.error { e.printStackTrace() }
            null
        }
    }

    /**
     * 永恒轮回官网新聞
     */
    fun news(id: String): String? {
        val requestProfile = RequestController(EternalReturnOfficialAPI.news(id))
        val resp = requestProfile.request()
        if (resp.status != 200) {
            return null
        }
        return resp?.body()
    }


    /**
     * 获取详细对局信息
     * @param id 对局id
     * @param nickname
     * @param seasonId 赛季id
     */
    suspend fun getMatchesById(id: String, nickname: String, seasonId: Int): EternalReturnMatchesById? {
        val resp = HTTPRequest.requestRetry(
            EternalReturnDakGGAPI.Player.matchById(
                nickname = nickname,
                seasonId = seasonId,
                id = id
            )
        )
        return resp?.body().to<EternalReturnMatchesById>()
    }


    /**
     * 天赋图片
     *
     * 持久化存储 应当缓存图片
     * @param id 需传递图片数字id
     * @return 磁盘存储路径
     */
    suspend fun getTraitSkillsIcon(id: Long): EternalReturnTraitSkillImgDTO {
        val skillPath = PathUtils.getEternalReturnDataImagePath("ico/TraitSkillsIcon/${id}.png")
        val traitSkills = getTraitSkills()
        traitSkills!!.let { skills ->
            val skill = skills.traitSkills.first { it.id == id }
            val skillGroup = skills.traitSkillGroups.firstOrNull { skill.group == it.key }
            var skillGroupPath: String? = null
            skillGroup?.let {
                skillGroupPath = PathUtils.getEternalReturnDataImagePath("ico/TraitSkillsIcon/${skillGroup.key}.png")
                if (!File(skillGroupPath).exists()) {
                    EternalReturnDakGGAPI.Download.downloadUrlStream(skillGroup.imageUrl, skillGroupPath)
                }
            }
            if (!File(skillPath).exists()) {
                EternalReturnDakGGAPI.Download.downloadUrlStream(skill.imageUrl, skillPath)
            }
            return EternalReturnTraitSkillImgDTO(skill = skillPath, skillGroup = skillGroupPath)
        }
    }


    /**
     * 当前赛季的天赋
     */
    fun getTraitSkills(): EternalReturnTraitSkills? {
        return redisUtils.getCache("Eternal_Return_Trait_Skills", EternalReturnTraitSkills::class.java, {
            val requestController = RequestController(
                EternalReturnDakGGAPI.Data.traitSkillsV1API()
            )
            val resp = HTTPRequest.requestRetry(requestController)
            resp?.body().to<EternalReturnTraitSkills>()
        }, 1L, TimeUnit.DAYS)
    }

    /**
     * 实验体技能(召唤师技能)
     * 闪灵、赤色风暴、激光陀螺
     */
    fun getTacticalSkills(): EternalReturnTacticalSkill? {
        return redisUtils.getCache("Eternal_Return_Tactical_Skills", EternalReturnTacticalSkill::class.java, {
            val requestController = RequestController(
                RequestDetailed().apply {
                    url = "https://er.dakgg.io/api/v1/data/tactical-skills?hl=zh-cn"
                    method = "GET"
                }
            )
            val resp = HTTPRequest.requestRetry(requestController)
            resp?.body().to<EternalReturnTacticalSkill>()
        }, 1L, TimeUnit.DAYS)
    }

    /**
     *  实验体技能(召唤师技能)图标
     */
    suspend fun getTacticalSkillIcon(id: Long): String {
        val skillPath = PathUtils.getEternalReturnDataImagePath("ico/TacticalSkillIcon/${id}.png")
        if (!File(skillPath).exists()) {
            getTacticalSkills()?.let { skill ->
                skill.tacticalSkills.first { it.id == id }.let { idSkill ->
                    EternalReturnDakGGAPI.Download.downloadUrlStream(idSkill.imageUrl, skillPath)
                }
            }
        }
        return skillPath
    }


    /**
     * 物品信息 包括英雄装备、武器
     */
    fun getItems(): EternalReturnItemInfos? {
        return redisUtils.getCache("Eternal_Return_Items", EternalReturnItemInfos::class.java, {
            val requestController = RequestController(
                RequestDetailed().apply {
                    url = "https://er.dakgg.io/api/v1/data/items?hl=zh-cn"
                    method = "GET"
                }
            )
            val resp = HTTPRequest.requestRetry(requestController)
            resp?.body().to<EternalReturnItemInfos>()
        })
    }

    /**
     * 装备图片
     * 持久化存储 应当缓存图片
     * @param id 需传递图片 数字id 204419
     * @return 磁盘存储路径
     */
    suspend fun getItemIcon(id: Long): String {
        val eternalReturnDataImagePath = PathUtils.getEternalReturnDataImagePath("ico/ItemIcon/${id}.png")
        if (!File(eternalReturnDataImagePath).exists()) {
            getItems()?.let { itemInfos ->
                EternalReturnDakGGAPI.Download.downloadUrlStream(
                    itemInfos.items.first { it.id == id }.imageUrl,
                    eternalReturnDataImagePath
                )
            }
        }
        return eternalReturnDataImagePath
    }


    /**
     * @param id  英雄id
     * @param characterImgUrlType 用于确定url类型以存储图片位置
     * @param skin 皮肤id
     * 持久化头像
     * 英雄图片
     */
    suspend fun getCharacterImg(
        id: Int,
        characterImgUrlType: EternalReturnCharacterById.CharacterImgUrlType,
        skin: Long = -1,
    ): String {
        val versionRegex = "(\\d+\\.\\d+\\.\\d+)".toRegex()
        val characterInfo = getCharacterInfo(id.toString())
        val eternalReturnDataImagePath =
            PathUtils.getEternalReturnDataImagePath("ico/${characterImgUrlType.type}/${id}/${skin}.png")
        if (!File(eternalReturnDataImagePath).exists()) {
            characterInfo.let {
                val url: String = if (skin != -1L) {
                    val skinInfo = it.skins.first { skinObj -> skinObj.id == skin }
                    when (characterImgUrlType) {
                        EternalReturnCharacterById.CharacterImgUrlType.CharProfileImageUrl ->
                            "//cdn.dak.gg/assets/er/game-assets/${versionRegex.find(skinInfo.imageUrl)!!.value}/CharProfile_${skinInfo.imageName}.png"

                        else ->
                            skinInfo.imageUrl
                    }
                } else {
                    when (characterImgUrlType) {
                        EternalReturnCharacterById.CharacterImgUrlType.ResultImageUrl ->
                            it.skins.first { skinObj -> skinObj.grade == 1 }.imageUrl

                        EternalReturnCharacterById.CharacterImgUrlType.CommunityImageUrl ->
                            it.communityImageUrl

                        EternalReturnCharacterById.CharacterImgUrlType.CharProfileImageUrl ->
                            "//cdn.dak.gg/assets/er/game-assets/${versionRegex.find(it.imageUrl)!!.value}/CharProfile_${it.imageName}.png"

                        else -> it.skins.first { skinObj -> skinObj.grade == 1 }.imageUrl
                    }
                }

                EternalReturnDakGGAPI.Download.downloadUrlStream(url, eternalReturnDataImagePath)
            }
        }
        return eternalReturnDataImagePath
    }

    /**
     * 所有武器信息
     */
    fun getWeapons(): EternalReturnWeapons? {
        return redisUtils.getCache("Eternal_Return: weapons", EternalReturnWeapons::class.java, {
            val resp = HTTPRequest.requestRetry(RequestController(EternalReturnDakGGAPI.Data.weaponV1API()))
            resp?.body().to<EternalReturnWeapons>()
        }, 2L, TimeUnit.DAYS)
    }

    /**
     * 武器图片
     * @param id
     * @return 存儲路径
     */
    suspend fun getWeaponIcon(id: Int): String {
        val eternalReturnDataImagePath =
            PathUtils.getEternalReturnDataImagePath("ico/WeaponsIcon/${id}.png")
        if (!File(eternalReturnDataImagePath).exists()) {
            getWeapons()?.let { weapons ->
                val iconUrl = weapons.masteries.first { it.id == id }.iconUrl
                EternalReturnDakGGAPI.Download.downloadUrlStream(iconUrl, eternalReturnDataImagePath)
            }
        }
        return eternalReturnDataImagePath
    }

    /**
     *  具体英雄信息 通过id、key、name获取
     *  无法获取到具体的英雄
     */
    suspend fun getCharacterInfo(id: String, retry: Boolean = true): EternalReturnCharacterById {
        characterFind()?.let { character ->
            if (id.matches(Regex("^\\d+$"))) {
                return character.characters.first { it.id == id.toInt() }
            }
            return character.characters.firstOrNull { it.key == id } ?: run {
                character.characters.first { it.name == id }
            }
        }
        if (retry) {
            redisUtils.deleteCache("Eternal_Return: characters")
            getCharacterInfo(id, false)
        }
        throw LoMuBotException("获取英雄信息失败")
    }

}