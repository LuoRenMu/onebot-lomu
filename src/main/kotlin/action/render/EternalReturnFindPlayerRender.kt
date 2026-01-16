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
import cn.luorenmu.config.external.LoMuProperties
import cn.luorenmu.exception.LoMuBotException
import cn.luorenmu.service.ImageService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * @author LoMu
 * Date 2025.04.22 21:13
 */
@Component
class EternalReturnFindPlayerRender(
    private val eternalReturnRequestData: EternalReturnRequestData,
    private val imageService: ImageService,
    private val render: FTLHRender,
    private val loMuProperties: LoMuProperties,
) {
    private val log = KotlinLogging.logger { }


    /**
     * @return 文件路径
     */
    fun imageRenderGenerate(nickname: String): String {
        val startTime = System.currentTimeMillis()
        val pageRender = runBlocking {
            pageRender(nickname)
        }
        log.info { "$nickname 网络数据请求耗时:${(System.currentTimeMillis() - startTime) / 1000}秒" }
        val imgPath = render.generateEternalReturnFindPlayerFTLHImage(pageRender)
        log.info { "$nickname 图片已生成 -> $imgPath" }
        return imgPath

    }

    suspend fun pageRender(nickname: String): EternalReturnRender {
        return coroutineScope {
            val currentSeason = eternalReturnRequestData.season().currentSeason
            val tiers = eternalReturnRequestData.tiers()
            val currentSeasonKey = currentSeason.key


            val profileDeferred = async(Dispatchers.IO) {
                eternalReturnRequestData.profile(nickname, currentSeasonKey)
            }
            val matchesDeferred = async(Dispatchers.IO) {
                eternalReturnRequestData.matches(nickname, currentSeasonKey)
            }


            val profile = profileDeferred.await()
            val matches = matchesDeferred.await()

            if (matches.matches.isEmpty()) {
                throw LoMuBotException("该玩家当前赛季不存在任何数据 -> $nickname")
            }
            //必要数据由left优先生成并渲染
            val eternalReturnRender = pageLeftConvert(profile, tiers, currentSeason)
            pageRightConvert(matches, eternalReturnRender)
            eternalReturnRender.rating = matchRating(matches)
            eternalReturnRender
        }
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
                    tierImageUrl = imageService.getTierImgUrl(currentTier.id)

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
                    )?.let { stats ->
                        imageService.getCharacterImgUrl(
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
                                    imageWrapperUrl = imageService.getCharacterImgUrl(
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
                                    imgUrl = imageService.getCharacterImgUrl(
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
                                            "%.1f", characterState.place / characterState.play.toDouble()
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
                            playerMMRStats =
                                EternalReturnRender.EternalReturnPlayerMMRStats(mmrDate = mmrStats.map { mmrs ->
                                    val dateStr = mmrs.first().toString().substring(4)
                                    dateStr.substring(0, 2) + "/" + dateStr.substring(2)
                                }, mmr = mmrStats.map { mmrs -> mmrs[1] })
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

            skillUrl = imageService.getTacticalSkillImgUrl(match.tacticalSkillGroup)
            traitSkillUrl = imageService.getTraitSkillImgUrl(match.traitFirstCore)
            traitSkillGroupUrl = imageService.getTraitSkillImgUrl(match.traitSecondSub.first(), true)
            equips = equipmentConvert(match.equipment.map { it.toLong() }.toList(), match.equipmentGrade)
            characterAvatarUrl = imageService.getCharacterImgUrl(
                EternalReturnCharacterById.CharacterImgUrlType.CharProfileImageUrl,
                match.characterNum.toInt(),
                match.skinCode
            )
            characterName = eternalReturnRequestData.getCharacterInfo(match.characterNum.toString()).name
            weaponUrl = imageService.getWeaponImgUrl(match.bestWeapon)


            // 队友
            val teammateInfos: MutableList<EternalReturnRender.EternalReturnPlayerMatchData.EternalReturnTeammate>? =
                teammate?.let { teamMateDataConvert(teammate, match.nickname) }

            teamMates = teammateInfos
        }
    }

    /**
     * 装备转换
     */
    private suspend fun equipmentConvert(
        equipment: List<Long>,
        equipmentGrade: List<Int>,
    ): MutableList<EternalReturnEquip> {
        val equips: MutableList<EternalReturnEquip> = mutableListOf()
        for (i in 0 until 5) {
            equips.add(
                i, EternalReturnEquip(
                    itemUrl = if (i < equipment.size) imageService.getItemImgUrl(equipment[i]) else "",
                    itemBgUrl = if (i < equipmentGrade.size) imageService.getItemImgBgUrl(equipmentGrade[i]) else ""
                )
            )
        }
        return equips
    }

    /**
     * 队友数据转换
     */
    private suspend fun teamMateDataConvert(
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
                        avatarUrl = imageService.getCharacterImgUrl(
                            EternalReturnCharacterById.CharacterImgUrlType.CharProfileImageUrl,
                            teamMate.characterNum.toInt(),
                            teamMate.skinCode
                        )
                        dmg = teamMate.damageToPlayer.toInt()
                        kill = teamMate.playerKill
                        assist = teamMate.playerAssistant
                        tk = teamMate.teamKill
                        rpImageUrl =
                            imageService.getTierImgUrl(teammate.playerTiers.first { iter -> iter.userNum.toLong() == teamMate.userNum }.tierId)
                        rp = teamMate.mmrAfter.toString()
                        skillUrl = imageService.getTacticalSkillImgUrl(teamMate.tacticalSkillGroup)
                        traitSkillUrl = imageService.getTraitSkillImgUrl(teamMate.traitFirstCore)
                        traitSkillGroupUrl = imageService.getTraitSkillImgUrl(teamMate.traitSecondSub.first(), true)
                        weaponUrl = imageService.getWeaponImgUrl(teamMate.bestWeapon)
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
    private fun matchRating(matches: EternalReturnMatches): String {
        val maxServer = matches.matches.groupBy { it.serverName }.maxBy { it.value.size }.value.first().serverName
        // 模式
        val mode = matches.matches.groupBy { it.matchingMode }.maxBy { it.value.size }.value.first().matchingMode
        val filter = matches.matches.filter { it.matchingMode == mode }
        val count = filter.count()
        val avg = filter.map { it.damageToPlayer }.average().toInt()
        val top1Count = filter.count { it.gameRank == 1 }
        val winRate = if (top1Count == 0) "0" else String.format("%.2f", top1Count.toDouble() / count.toDouble() * 100)
        return "常驻服务器${EternalReturnMatches.serverNameCovert(maxServer)} ${filter.first().matchTypeStr}模式 ${count}场对局 胜率:${winRate}% 平均伤害:${avg}"
    }


    private suspend fun pageRightConvert(
        matches: EternalReturnMatches,
        eternalReturnRender: EternalReturnRender,
    ) {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        coroutineScope {

            val nickname = matches.matches.first().nickname

            // 需要渲染队友的对局ID
            val matchIds =
                matches.matches.filter { match -> match.matchingMode == 3 }.take(loMuProperties.eter.teammate)
                    .map { it.gameId }

            val seasonId =
                eternalReturnRequestData.season().seasons.first { sea -> sea.key == matches.meta.season }.id

            val awaitMatchesByIds: MutableList<Deferred<EternalReturnMatchesById>> = mutableListOf()
            matchIds.forEach { matchId ->
                val async = async(Dispatchers.IO) {
                    eternalReturnRequestData.getMatchesById(
                        matchId.toString(),
                        nickname,
                        seasonId
                    )
                }
                awaitMatchesByIds.add(async)
            }

            val map = awaitMatchesByIds.awaitAll().associateBy { byId -> byId.matches.first().gameId }

            matches.matches.forEach { match ->
                try {
                    // 获取队友信息
                    matchIds.let {
                        eternalReturnRender.matches.add(matcherConvert(match, dateFormatter, map[match.gameId]))
                    }


                } catch (e: Exception) {
                    log.error { e.printStackTrace() }
                }
            }
        }
    }


}