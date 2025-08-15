package cn.luorenmu.action.render

import action.commandProcess.eternalReturn.entity.EternalReturnCharacterById
import action.commandProcess.eternalReturn.entity.EternalReturnSeasons
import action.commandProcess.eternalReturn.entity.profile.EternalReturnProfile
import action.commandProcess.eternalReturn.entity.profile.EternalReturnProfileStat
import cn.luorenmu.action.commandProcess.eternalReturn.entity.dto.EternalReturnEquip
import cn.luorenmu.action.commandProcess.eternalReturn.entity.dto.EternalReturnRender
import cn.luorenmu.action.commandProcess.eternalReturn.entity.dto.EternalReturnRender.EternalReturnPlayerData
import cn.luorenmu.action.commandProcess.eternalReturn.entity.dto.EternalReturnRender.EternalReturnPlayerRecentPlay
import cn.luorenmu.action.commandProcess.eternalReturn.entity.matcher.EternalReturnMatches
import cn.luorenmu.action.commandProcess.eternalReturn.entity.matcher.EternalReturnMatchesById
import cn.luorenmu.action.commandProcess.eternalReturn.entity.tier.EternalReturnTiers
import cn.luorenmu.action.request.EternalReturnRequestData
import cn.luorenmu.common.utils.FreeMarkerUtils
import cn.luorenmu.common.utils.PathUtils
import cn.luorenmu.common.utils.RedisUtils
import cn.luorenmu.core.WebPool
import cn.luorenmu.exception.LoMuBotException
import cn.luorenmu.service.ImageService
import com.mikuac.shiro.common.utils.MsgUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * @author LoMu
 * Date 2025.04.22 21:13
 */
@Component
class EternalReturnFindPlayerRender(
    private val eternalReturnRequestData: EternalReturnRequestData,
    private val redisUtils: RedisUtils,
    private val imageService: ImageService,
    private val webPool: WebPool,
    @Value("\${server.port}")
    private val port: String,
) {
    private val log = KotlinLogging.logger { }


    fun imageRenderGenerate(nickname: String): String {
        val pageRender = runBlocking { pageRender(nickname) }
        val userNum = pageRender.userNum

        val imgPath = PathUtils.getEternalReturnNicknameImagePath("render_$userNum")
        val returnMsg = MsgUtils.builder().img(imgPath).build()

        try {
            val parseData = FreeMarkerUtils.parseData("eternal_return_player.ftlh", pageRender)
            log.info { "$nickname 页面图片已生成" }
            redisUtils.setCache("ftlh:eternal_return_player_data_${userNum}", parseData, 5L, TimeUnit.MINUTES)
            webPool.getWebPageScreenshot()
                .screenshotSelector(
                    "http://localhost:$port/ftlh/eternal_return_player_data_${userNum}",
                    imgPath,
                    "#content-container"
                )
            redisUtils.setCache("nickname:${nickname}", returnMsg, 5L, TimeUnit.MINUTES)
            return returnMsg
        } catch (e: Exception) {
            throw LoMuBotException("无法为其生成数据 -> $nickname")
        }
    }

    suspend fun pageRender(nickname: String): EternalReturnRender {
        val currentSeason = eternalReturnRequestData.currentSeason()?.currentSeason

        val currentSeasonKey = currentSeason?.key ?: run {
            throw LoMuBotException("无法获取当前赛季")
        }
        val profile = eternalReturnRequestData.profile(nickname, currentSeasonKey)
        val tiers = eternalReturnRequestData.tiers()
        val matches = eternalReturnRequestData.matches(nickname, currentSeasonKey)

        if (profile == null || tiers == null || matches == null) {
            throw LoMuBotException("多次尝试仍然无法从dak.gg获取数据")
        }


        if (matches.matches.isEmpty()) {
            throw LoMuBotException("该玩家当前赛季不存在任何数据 -> $nickname")
        }
        //必要数据由left优先生成并渲染
        val eternalReturnRender = pageLeftConvert(profile, tiers, currentSeason)
        pageRightConvert(matches, eternalReturnRender)
        eternalReturnRender.rating = matchRating(matches)
        return eternalReturnRender

    }


    suspend fun pageLeftConvert(
        profile: EternalReturnProfile,
        tiers: EternalReturnTiers,
        season: EternalReturnSeasons,
    ): EternalReturnRender {
        val player = profile.player
        val playerSeasons = profile.playerSeasons
        val playerSeasonOverviews = profile.playerSeasonOverviews
        val recentPlays = mutableListOf<EternalReturnPlayerRecentPlay>()
        val characterUseStats = mutableListOf<EternalReturnRender.EternalReturnCharacterUseStats>()
        var profileImageUrl: String? = null
        var playerMMRStats: EternalReturnRender.EternalReturnPlayerMMRStats? = null
        var playTime: Long = 0
        val eternalReturnPlayerData = EternalReturnPlayerData().apply {
            if (playerSeasons.isNotEmpty()) {
                // 选取最新的段位信息 并且装配部分数据
                playerSeasons.firstOrNull { it.seasonId == season.id }?.let { data ->
                    val currentTier = data.tierId.let { tierID -> tiers.tiers.first { it.id == tierID } }
                    // example: 1234RP or 段位鉴定中.
                    if (data.mmr != 0) {
                        rp = data.mmr.let { mmr -> mmr.toString() + "RP" }
                    }
                    // example: 灭钻 2 - 209  无暇 -209
                    if (currentTier.id != 0) {
                        val tierGrad = if (data.tierId > 6 && data.tierId * 10 > 60) "" else data.tierGradeId
                        rpName = "${currentTier.name}$tierGrad - ${data.tierMmr}RP"
                    }
                    tierImageUrl = getTierImgUrl(currentTier.id)

                }



                playerSeasonOverviews?.let { seasonOverviews ->
                    val firstSeasonOverview = seasonOverviews.firstOrNull()

                    //筛选出排位信息
                    seasonOverviews.firstOrNull { seasonOverview -> seasonOverview.matchingModeId == 3 }
                        ?.let { seasonOverview ->
                            play = seasonOverview.play
                            val playDouble = play.toDouble()
                            if (play != 0) {
                                avgTk = String.format("%.2f", seasonOverview.teamKill / playDouble)
                                avgKill = String.format("%.2f", seasonOverview.playerKill / playDouble)
                                avgAssists = String.format("%.2f", seasonOverview.playerAssistant / playDouble)
                                avgDmg = (seasonOverview.damageToPlayer / play).toString()
                                avgRank = "#" + String.format("%.2f", seasonOverview.place / playDouble)
                                top1 = String.format("%.1f", (seasonOverview.win / playDouble) * 100) + "%"
                                top2 = String.format("%.1f", (seasonOverview.top2 / playDouble) * 100) + "%"
                                top3 = String.format("%.1f", (seasonOverview.top3 / playDouble) * 100) + "%"
                            }
                        }


                    //主页图
                    profileImageUrl = firstSeasonOverview?.characterStats?.maxByOrNull(
                        EternalReturnProfileStat::play
                    )
                        ?.let { stats ->
                            getCharacterImgUrl(
                                EternalReturnCharacterById.CharacterImgUrlType.ResultImageUrl,
                                stats.key.toInt(),
                                stats.skinStats?.maxByOrNull(EternalReturnProfileStat::play)?.key ?: -1L
                            )
                        }

                    //近期一起玩的人
                    seasonOverviews.firstOrNull { seasonOverview -> seasonOverview.duoStats.isNotEmpty() }
                        ?.let { seasonOverview ->
                            seasonOverview.duoStats.take(8).forEach { duoStat ->
                                recentPlays.add(EternalReturnPlayerRecentPlay().apply {
                                    imageWrapperUrl = getCharacterImgUrl(
                                        EternalReturnCharacterById.CharacterImgUrlType.CharProfileImageUrl,
                                        duoStat.characterStats.first().key.toInt(),
                                    )
                                    this.plays = duoStat.play
                                    val playDouble = this.plays.toDouble()
                                    this.nickname = duoStat.nickname

                                    this.winRate = "${String.format("%.1f", (duoStat.win / playDouble) * 100)}%"
                                    this.avgRank = "#${String.format("%.1f", duoStat.place / playDouble)}"
                                })
                            }
                        }

                    // 常用排位角色
                    seasonOverviews.firstOrNull { it.matchingModeId == 3 }?.characterStats?.take(8)
                        ?.forEach { characterState ->
                            val character = eternalReturnRequestData.getCharacterInfo(characterState.key.toString())
                            characterUseStats.add(
                                EternalReturnRender.EternalReturnCharacterUseStats(
                                    characterName = character.name,
                                    imgUrl = getCharacterImgUrl(
                                        EternalReturnCharacterById.CharacterImgUrlType.CharProfileImageUrl,
                                        characterState.key.toInt()
                                    ),
                                    winRate = "${
                                        String.format(
                                            "%.1f",
                                            if (characterState.win == 0L) 0.0 else characterState.win / characterState.play.toDouble() * 100
                                        )
                                    }%",
                                    characterPlay = characterState.play,
                                    getRP = characterState.mmrGain,
                                    avgRank = "#${
                                        String.format(
                                            "%.1f",
                                            characterState.place / characterState.play.toDouble()
                                        )
                                    }",
                                    avgDmg = if (characterState.damageToPlayer == 0) 0 else characterState.damageToPlayer / characterState.play,
                                )
                            )
                        }

                    // 分数曲线
                    firstSeasonOverview?.let { seasonOverview ->
                        if (seasonOverview.mmrStats.isNotEmpty()) {
                            val mmrStats = seasonOverview.mmrStats.take(7).reversed()
                            playerMMRStats = EternalReturnRender.EternalReturnPlayerMMRStats(
                                mmrDate = mmrStats.map { mmrs ->
                                    val dateStr = mmrs.first().toString().substring(4)
                                    dateStr.substring(0, 2) + "/" + dateStr.substring(2)
                                },
                                mmr = mmrStats.map { mmrs -> mmrs[1] }
                            )
                        }
                    }

                    // 游戏时间、单位为秒
                    playTime = firstSeasonOverview?.playTime ?: 0
                }
            }
        }



        return EternalReturnRender(
            userNum = player.userNum,
            nickName = player.name,
            level = player.accountLevel,
            eternalReturnPlayerData,
            profileImageUrl,
            mmrStats = playerMMRStats,
            recentPlayers = recentPlays,
            season = season.name,
            playTime = playTime,
            characterUseStats = characterUseStats
        )
    }

    private suspend fun matcherConvert(
        match: EternalReturnMatches.Match,
        dateFormatter: DateTimeFormatter,
        teammate: EternalReturnMatchesById?,
    ): EternalReturnRender.EternalReturnPlayerMatchData {
        return EternalReturnRender.EternalReturnPlayerMatchData().apply {
            type = match.matchTypeStr
            rank = if (match.escapeState == 3) 99 else match.gameRank
            gameId = match.gameId.toString()
            serverName = match.serverName
            version = "1.${match.versionMajor}.${match.versionMinor}"
            kill = match.playerKill
            assist = match.playerAssistant
            dmg = match.damageToPlayer
            tk = match.teamKill
            rp = match.mmrAfter
            rpChange = match.mmrGain
            val killAndAssist = kill + assist
            kda =
                if (match.playerDeaths == 0) killAndAssist.toDouble() else killAndAssist.toDouble() / match.playerDeaths
            routeId = if (match.routeIdOfStart != 0L) match.routeIdOfStart.toString() else "Private"
            val date = ZonedDateTime.parse(match.startDtm, dateFormatter)
            dateHour = "${date.hour}:${date.minute}:${date.second}"
            dateMonth = "${date.monthValue}月${date.dayOfMonth}日"

            skillUrl = getTacticalSkillImgUrl(match.tacticalSkillGroup)
            traitSkillUrl = getTraitSkillImgUrl(match.traitFirstCore)
            traitSkillGroupUrl = getTraitSkillImgUrl(match.traitSecondSub.first(), true)
            equips = equipmentConvert(match.equipment.map { it.toLong() }.toList(), match.equipmentGrade)
            characterAvatarUrl =
                getCharacterImgUrl(
                    EternalReturnCharacterById.CharacterImgUrlType.CharProfileImageUrl,
                    match.characterNum.toInt(),
                    match.skinCode
                )
            characterName =
                eternalReturnRequestData.getCharacterInfo(match.characterNum.toString()).name
            weaponUrl = getWeaponImgUrl(match.bestWeapon)


            // 队友
            val teammateInfos: MutableList<EternalReturnRender.EternalReturnPlayerMatchData.EternalReturnTeammate>? =
                teammate?.let { teamMateDataConvert(teammate, match.nickname) }

            teamMates = teammateInfos
        }
    }

    /**
     * 装备转换
     */
    private fun equipmentConvert(equipment: List<Long>, equipmentGrade: List<Int>): MutableList<EternalReturnEquip> {
        val equips: MutableList<EternalReturnEquip> = mutableListOf()
        for (i in 0 until 5) {
            equips.add(
                i,
                EternalReturnEquip(
                    itemUrl = if (i < equipment.size) getItemImgUrl(equipment[i]) else "",
                    itemBgUrl = if (i < equipmentGrade.size) getItemImgBgUrl(equipmentGrade[i]) else ""
                )
            )
        }
        return equips
    }

    /**
     * 队友数据转换
     */
    private fun teamMateDataConvert(
        teammate: EternalReturnMatchesById,
        nickname: String,
    ): MutableList<EternalReturnRender.EternalReturnPlayerMatchData.EternalReturnTeammate> {
        val teammateInfos: MutableList<EternalReturnRender.EternalReturnPlayerMatchData.EternalReturnTeammate> =
            mutableListOf()
        teammate.let {
            val selfInfo = teammate.matches.first { matchById -> matchById.nickname == nickname }
            val teamNumber = selfInfo.teamNumber
            val teamMates =
                teammate.matches.filter { matchById -> matchById.teamNumber == teamNumber && matchById.nickname != nickname }

            teamMates.forEach { teamMate ->
                teammateInfos.add(
                    EternalReturnRender.EternalReturnPlayerMatchData.EternalReturnTeammate().apply {
                        nickName = teamMate.nickname
                        avatarUrl = getCharacterImgUrl(
                            EternalReturnCharacterById.CharacterImgUrlType.CharProfileImageUrl,
                            teamMate.characterNum.toInt(),
                            teamMate.skinCode
                        )
                        dmg = teamMate.damageToPlayer.toInt()
                        kill = teamMate.playerKill
                        assist = teamMate.playerAssistant
                        tk = teamMate.teamKill
                        rpImageUrl =
                            getTierImgUrl(teammate.playerTiers.first { iter -> iter.userNum.toLong() == teamMate.userNum }.tierId)
                        rp = teamMate.mmrAfter.toString()
                        skillUrl = getTacticalSkillImgUrl(teamMate.tacticalSkillGroup)
                        traitSkillUrl = getTraitSkillImgUrl(teamMate.traitFirstCore)
                        traitSkillGroupUrl = getTraitSkillImgUrl(teamMate.traitSecondSub.first(), true)
                        weaponUrl = getWeaponImgUrl(teamMate.bestWeapon)
                        equips =
                            equipmentConvert(teamMate.equipment.map { it.toLong() }.toList(), teamMate.equipmentGrade)
                    })
            }
        }
        return teammateInfos
    }


    /**
     * 对局评价
     */
    private fun matchRating(matches: EternalReturnMatches): String? {
        val maxServer = matches.matches.groupBy { it.serverName }.maxBy { it.value.size }.value.first().serverNameStr
        val maxMatchType =
            matches.matches.groupBy { it.matchTypeStr }.maxBy { it.value.size }.value.first().matchTypeStr
        val matchesData = matches.matches.filter { it.matchTypeStr == maxServer }
        val filterCount = matchesData.count()
        val top1Count = matchesData.filter { it.gameRank == 1 }.size
        val winRate = String.format("%.2f", (top1Count.toDouble() / filterCount.toDouble()) * 100)
        return "常驻服务器${maxServer}:${maxMatchType}模式:${filterCount}场对局:胜率:${winRate}%"
    }


    private suspend fun pageRightConvert(
        matches: EternalReturnMatches,
        eternalReturnRender: EternalReturnRender,
    ) {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        coroutineScope {
            // 最近一场的排位信息
            val firstMatchId = matches.matches.firstOrNull { match -> match.matchTypeStr == "排位" }?.gameId
            matches.matches
                .forEach { match ->
                    try {
                        // 获取队友信息
                        val teammate = firstMatchId?.let {
                            if (match.gameId == firstMatchId) {
                                val seasonID =
                                    eternalReturnRequestData.currentSeason()?.seasons?.first { sea -> sea.key == matches.meta.season }?.id
                                eternalReturnRequestData.getMatchesById(
                                    match.gameId.toString(),
                                    match.nickname,
                                    seasonID ?: 0
                                )
                            } else null
                        }

                        eternalReturnRender.matches.add(matcherConvert(match, dateFormatter, teammate))
                    } catch (e: Exception) {
                        log.error { e.printStackTrace() }
                    }
                }
        }
    }


    private fun getCharacterImgUrl(type: EternalReturnCharacterById.CharacterImgUrlType, id: Int, skin: Long = -1) =
        run {
            imageService.getEternalReturnCharacterImage(type, id, skin)
            "/images/eternal_return/character/$type/$id/$skin"
        }

    private fun getItemImgUrl(id: Long) = run {
        imageService.getEternalReturnItemImage(id)
        "/images/eternal_return/item/${id}"
    }

    private fun getTierImgUrl(id: Int) = run {
        imageService.getTierImage(id)
        "/images/eternal_return/tier/${id}"
    }

    private fun getItemImgBgUrl(id: Int) = run {
        imageService.getEternalReturnItemBgImage(id)
        "/images/eternal_return/item_bg/${id}"
    }

    private fun getTraitSkillImgUrl(id: Long, `is`: Boolean = false) = run {

        imageService.getEternalReturnTraitSkillImage(id)
        "/images/eternal_return/trait_skill/${id}?is=${`is`}"

    }

    private fun getTacticalSkillImgUrl(id: Long) = run {
        imageService.getEternalReturnTacticalSkillImage(id)
        "/images/eternal_return/tactical_skill/${id}"
    }

    private fun getWeaponImgUrl(id: Int) = run {
        imageService.getEternalReturnWeaponImage(id)
        "/images/eternal_return/weapon/${id}"
    }


}