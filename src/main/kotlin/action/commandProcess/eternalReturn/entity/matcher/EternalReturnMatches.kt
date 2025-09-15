package cn.luorenmu.action.commandProcess.eternalReturn.entity.matcher

import com.alibaba.fastjson2.annotation.JSONField
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2025.04.06 15:18
 */
@Serializable
data class EternalReturnMatches(
    val meta: Meta,
    val matches: List<Match>,
) {
    @Serializable
    data class Match(
        // 装备
        // https://cdn.dak.gg/assets/er/game-assets/1.44.0/ItemIcon_115504.png
        @SerialName("equipment")
        @Contextual
        @JSONField(name = "equipment")
        val equipmentVirtual: Any,
        // 装备背景
        // https://cdn.dak.gg/er/images/item/ico-itemgradebg-04.svg
        @JSONField(name = "equipmentGrade")
        val equipmentGradeVirtual: Any,
        val userNum: Long = 0,
        val nickname: String = "",
        val gameId: Long = 0,
        val seasonId: Long = 0,
        val matchingMode: Int = 0,
        val matchingTeamMode: Long = 0,
        val characterNum: Long = 0,
        val skinCode: Long = 0,
        val characterLevel: Long = 0,
        val squadRumbleRank: Int = 0,
        val gameRank: Int = 0,
        val playerKill: Int = 0,
        val playerDeaths: Int = 0,
        val playerAssistant: Int = 0,
        val monsterKill: Long = 0,
        // 这局之前的分数
        val mmrBefore: Int = 0,
        // 这局之后的分数
        val mmrAfter: Int = 0,
        // 这局加了/减了多少分
        val mmrGain: Int = 0,
        // 武器
        // https://er.dakgg.io/api/v1/data/masteries?hl=en
        val bestWeapon: Int = 0,
        val bestWeaponLevel: Int = 0,
        val masteryLevel: Map<String, Long>,
        val versionMajor: Long = 0,
        val versionMinor: Long = 0,
        val serverName: String = "",
        val criticalStrikeDamage: Long = 0,
        val coolDownReduction: Double = 0.0,
        val lifeSteal: Double = 0.0,
        val normalLifeSteal: Double = 0.0,
        val skillLifeSteal: Long = 0,
        val amplifierToMonster: Double = 0.0,
        val bonusExp: Long = 0,
        val startDtm: String = "",
        val duration: Long = 0,
        val playTime: Long = 0,
        val watchTime: Long = 0,
        val totalTime: Long = 0,
        val survivableTime: Long = 0,
        val botAdded: Long = 0,
        val botRemain: Long = 0,
        val restrictedAreaAccelerated: Long = 0,
        val safeAreas: Long = 0,
        val teamNumber: Long = 0,
        val preMade: Long = 0,
        val gainedNormalMmrKFactor: Double = 0.0,
        val victory: Long = 0,
        val craftUncommon: Long = 0,
        val craftRare: Long = 0,
        val craftEpic: Long = 0,
        val craftLegend: Long = 0,
        val damageToPlayer: Long = 0,
        val damageFromPlayerItemSkill: Long = 0,
        val damageFromPlayerDirect: Long = 0,
        val damageFromPlayerUniqueSkill: Long = 0,
        val healAmount: Long = 0,
        val teamRecover: Long = 0,
        val protectAbsorb: Long = 0,
        val addSurveillanceCamera: Long = 0,
        val addTelephotoCamera: Long = 0,
        val removeSurveillanceCamera: Long = 0,
        val removeTelephotoCamera: Long = 0,
        val useHyperLoop: Long = 0,
        val useSecurityConsole: Long = 0,
        val giveUp: Long = 0,
        val teamSpectator: Long = 0,
        val pcCafe: Long = 0,
        val routeIdOfStart: Long = 0,
        val routeSlotId: Long = 0,
        val placeOfStart: String = "",
        val matchSize: Long = 0,
        val teamKill: Int = 0,
        val fishingCount: Long = 0,
        val useEmoticonCount: Long = 0,
        val expireDtm: String = "",
        // 主要天赋技能
        // https://er.dakgg.io/api/v1/data/trait-skills?hl=zh-cn
        val traitFirstCore: Long = 0,
        val traitSecondSub: List<Long>,
        val rankPoint: Int = 0,
        val scoredPoint: List<Long>,
        val killDetails: String = "",
        val deathDetails: String = "",
        val deathsPhaseOne: Long = 0,
        val deathsPhaseTwo: Long = 0,
        val deathsPhaseThree: Long = 0,
        val usedPairLoop: Long = 0,
        val ccTimeToPlayer: Double = 0.0,
        val creditSource: Map<String, Double>?,
        val boughtInfusion: String = "",
        val itemTransferredConsole: List<Long>,
        val itemTransferredDrone: List<Long>,
        val escapeState: Int = 0,
        val totalExtraKill: Long = 0,
        val collectItemForLog: List<Long>,
        val equipFirstItemForLog: List<List<Long>>,
        // 战术技能  闪灵、赤色风暴
        //https://er.dakgg.io/api/v1/data/tactical-skills?hl=zh-cn
        val tacticalSkillGroup: Long = 0,
        val tacticalSkillLevel: Long = 0,
        val teamDown: Long = 0,
        val teamBattleZoneDown: Long = 0,
        val teamRepeatDown: Long = 0,
        val skillAmp: Long = 0,
        val isLeavingBeforeCreditRevivalTerminate: Boolean,
        val mmrGainInGame: Long = 0,
        val mmrLossEntryCost: Long = 0,
    ) {
        /**
         * 沟槽的dak.gg返回数据不一致
         */
        private inline fun <reified T> convertToList(value: Any?): List<T> {
            return when (value) {
                is List<*> -> value.mapNotNull { it as? T }
                is Map<*, *> -> {
                    val list = mutableListOf<T>()
                    value.forEach { (key, v) -> list.add(key.toString().toInt(), v as T) }
                    list
                }

                else -> emptyList()
            }
        }

        val equipment: List<Int>
            get() {
                return convertToList(equipmentVirtual)
            }
        val equipmentGrade: List<Int>
            get() {
                return convertToList(equipmentGradeVirtual)
            }


        // 3为排位模式，2为匹配模式 6为钴协议 8 为联盟 9 为孤狼 0为全部
        val matchTypeStr: String =
            matchModeTypeCovert(matchingMode)
        val serverNameStr: String = serverNameCovert(serverName)
    }


    companion object {
        fun matchModeTypeCovert(matchingMode: Int) =
            when (matchingMode) {
                2 -> "匹配"
                3 -> "排位"
                6 -> "钴协议"
                8 -> "联盟"
                9 -> "孤狼"
                else -> "未知"
            }

        fun serverNameCovert(serverName: String) =
            when (serverName) {
                "Asia" -> "亚一"
                "Asia2" -> "亚二"
                "NorthAmerica" -> "北美"
                "Europe" -> "欧洲"
                "SouthAmerica" -> "南美"
                "Australia" -> "澳一"
                else -> serverName
            }
    }

    @Serializable
    data class Meta(
        val season: String = "",
        val matchingMode: String = "",
        val teamMode: String = "",
        val page: Long = 0,
        val count: Long = 0,
    )
}
