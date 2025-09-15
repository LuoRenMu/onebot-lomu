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
import cn.luorenmu.action.request.entity.EternalReturnTraitSkillImgDTO
import cn.luorenmu.common.utils.HTTPRequestUtil
import cn.luorenmu.common.utils.PathUtils
import cn.luorenmu.exception.LoMuBotException
import com.alibaba.fastjson2.to
import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.io.File

/**
 * @author LoMu
 * Date 2024.08.03 9:11
 */
@Component
class EternalReturnRequestData {


    // sync player
    suspend fun syncPlayers(nickname: String, counter: Int = 0): Boolean {
        if (counter == 3) {
            return true
        }
        val resp = HTTPRequestUtil.call(EternalReturnDakGGAPI.Player.syncDataV0API(nickname))
        try {
            val body = resp.body<String>()
            if (body.contains("retry_after")) {
                return syncPlayers(nickname, counter + 1)
            }
            if (body.contains("invalid name")) {
                return false
            }
            return !body.contains("not_found")

        } catch (_: Exception) {
            return true
        }
    }

    /**
     * 段位总览(有哪些段位?)
     */
    fun tiers(): EternalReturnTiers {
        return HTTPRequestUtil.requestCacheJson<EternalReturnTiers>("Tiers", EternalReturnDakGGAPI.Data.tiersV1API())
    }

    /**
     * 匹配 不缓存数据
     */
    suspend fun matches(
        nickname: String,
        season: String,
        matchingMode: String = "ALL",
        teamMode: String = "ALL",
        page: Int = 1,
    ): EternalReturnMatches {
        return HTTPRequestUtil.callDTO<EternalReturnMatches>(
            EternalReturnDakGGAPI.Player.matchesV1API(
                nickname = nickname,
                season = season,
                matchingMode = matchingMode,
                teamMode = teamMode,
                page = page,
            )
        )
    }

    /**
     * 段位分布
     */
    fun tierDistributionsFind(): EternalReturnTierDistributions {
        return HTTPRequestUtil.requestCacheJson<EternalReturnTierDistributions>(
            "TierDistributions",
            EternalReturnDakGGAPI.Statistics.tierDistributionV0API()
        )
    }


    fun leaderboardFind(): EternalReturnLeaderboard {
        return season().let { season ->
            val leaderboard = HTTPRequestUtil.requestCacheJson<EternalReturnLeaderboard>(
                "Leaderboard",
                EternalReturnDakGGAPI.Leaderboard.leaderboardV0API(season.currentSeason.key)
            )
            leaderboard.currentSeason = season
            leaderboard
        }
    }


    fun characterFind(): EternalReturnCharacter {
        return HTTPRequestUtil.requestCacheJson<EternalReturnCharacter>(
            "Characters",
            EternalReturnDakGGAPI.Data.charactersV1API()
        )
    }

    fun season(): EternalReturnSeason {
        return HTTPRequestUtil.requestCacheJson<EternalReturnSeason>("Season", EternalReturnDakGGAPI.Data.seasonV1API())
    }


    /**
     * 玩家信息 不缓存
     */
    suspend fun profile(name: String, season: String = ""): EternalReturnProfile {
        val resp = HTTPRequestUtil.call(
            EternalReturnDakGGAPI.Player.profileV1API(
                name,
                season
            )
        )
        if (resp.status.value == 404) {
            throw LoMuBotException("不存在的玩家 -> $name")
        }
        return resp.bodyAsText().to<EternalReturnProfile>()
    }

    /**
     * 永恒轮回官网新聞
     */
    fun news(id: String): String {
        return runBlocking { HTTPRequestUtil.callDTO<String>(EternalReturnOfficialAPI.news(id)) }

    }


    /**
     * 获取详细对局信息
     * @param id 对局id
     * @param nickname
     * @param seasonId 赛季id
     */
    suspend fun getMatchesById(id: String, nickname: String, seasonId: Int): EternalReturnMatchesById {
        return HTTPRequestUtil.requestCacheJson<EternalReturnMatchesById>(
            "MatchesID:$id",
            EternalReturnDakGGAPI.Player.matchById(
                nickname = nickname,
                seasonId = seasonId,
                id = id
            )
        )
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
        traitSkills.let { skills ->
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
    fun getTraitSkills(): EternalReturnTraitSkills {
        return HTTPRequestUtil.requestCacheJson<EternalReturnTraitSkills>(
            "Trait",
            EternalReturnDakGGAPI.Data.traitSkillsV1API()
        )

    }

    /**
     * 实验体技能(召唤师技能)
     * 闪灵、赤色风暴、激光陀螺
     */
    fun getTacticalSkills(): EternalReturnTacticalSkill {
        return HTTPRequestUtil.requestCacheJson<EternalReturnTacticalSkill>(
            "TacticalSkills",
            EternalReturnDakGGAPI.Data.tacticalSkillsV1API()
        )
    }

    /**
     *  实验体技能(召唤师技能)图标
     */
    suspend fun getTacticalSkillIcon(id: Long): String {
        val skillPath = PathUtils.getEternalReturnDataImagePath("ico/TacticalSkillIcon/${id}.png")
        if (!File(skillPath).exists()) {
            val skill = getTacticalSkills()
            skill.tacticalSkills.first { it.id == id }.let { idSkill ->
                EternalReturnDakGGAPI.Download.downloadUrlStream(idSkill.imageUrl, skillPath)
            }
        }
        return skillPath
    }


    /**
     * 物品信息 包括英雄装备、武器
     */
    fun getItems(): EternalReturnItemInfos? {
        return HTTPRequestUtil.requestCacheJson<EternalReturnItemInfos>(
            "ItemInfos",
            EternalReturnDakGGAPI.Data.itemInfosV1API()
        )
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
    fun getWeapons(): EternalReturnWeapons {
        return HTTPRequestUtil.requestCacheJson<EternalReturnWeapons>(
            "Weapons",
            EternalReturnDakGGAPI.Data.weaponV1API()
        )
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
            getWeapons().let { weapons ->
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
        characterFind().let { character ->
            var characterInfo: EternalReturnCharacterById? = null

            // 是英雄数字ID
            if (id.matches(Regex("^\\d+$"))) {
                characterInfo = character.characters.firstOrNull { it.id == id.toInt() }
            }
            characterInfo ?: run {
                characterInfo = character.characters.firstOrNull { it.key == id } ?: run {
                    character.characters.firstOrNull { it.name == id }
                }
            }
            characterInfo?.let {
                return it
            }

            // 角色肯能发生了更新
            if (retry) {
                HTTPRequestUtil.jsonCache.invalidate("Characters")
                return getCharacterInfo(id, false)
            }
            throw LoMuBotException("获取英雄信息失败")
        }
    }

}