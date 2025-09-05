package cn.luorenmu.action.request.api

import cn.hutool.http.HttpResponse
import cn.luorenmu.common.utils.PathUtils
import cn.luorenmu.entity.RequestEntity.RequestDetailed
import cn.luorenmu.file.ReadWriteFile
import java.io.File

/**
 *
 * @author LoMu
 * Date 2025/8/24 07:35
 */
object EternalReturnDakGGAPI {
    object PageURL {
        fun playerPageURL(nickname: String) = "https://dak.gg/er/players/${nickname}?gameMode=ALL"

        const val ROUTES_URL = "https://dak.gg/er/routes/"

        const val CHARACTER_STATISTICS_URL = "https://dak.gg/er/statistics"
        fun characterPageURL(
            characterName: String,
            teamMode: String = "SQUAD",
            weaponType: String,
            period: Int = 3,
            tier: String = "diamond_plus",
        ) =
            "https://dak.gg/er/characters/${characterName}?teamMode=${teamMode}&weaponType=${weaponType}&period=${period}day&tier=${tier}"
    }


    object Player {
        fun syncDataV0API(nickname: String) = RequestDetailed().apply {
            url = "https://er.dakgg.io/api/v0/rpc/player-sync/by-name/${nickname}"
            method = "GET"
        }


        fun profileV1API(nickname: String, season: String) = RequestDetailed().apply {
            url = "https://er.dakgg.io/api/v1/players/${nickname}/profile?season=${season}"
            method = "GET"
        }

        fun matchesV1API(nickname: String, season: String, matchingMode: String, teamMode: String, page: Int = 1) =
            RequestDetailed().apply {
                url =
                    "https://er.dakgg.io/api/v1/players/${nickname}/matches?season=${season}&matchingMode=${matchingMode}&teamMode=${teamMode}&page=${page}&hl=zh_CN"
                method = "GET"
            }

        fun matchById(id: String, nickname: String, seasonId: Int) = RequestDetailed().apply {
            url =
                "https://er.dakgg.io/api/v1/players/$nickname/matches/$seasonId/$id"
            method = "get"
        }

    }


    object Leaderboard {
        fun leaderboardV0API(
            season: String,
            serverName: String = "seoul",
            teamMode: String = "SQUAD",
            page: Int = 1,
        ) =
            RequestDetailed().apply {
                url =
                    "https://er.dakgg.io/api/v0/leaderboard?page=${page}&seasonKey=${season}&serverName=${serverName}&teamMode=${teamMode}&hl=zh_CN"
                method = "GET"
            }

        fun leaderboardCharactersV0API(
            character: String,
            season: String = "season",
            teamMode: String,
            sortType: String,
            page: Int = 1,
        ) = RequestDetailed().apply {
            url =
                "https://er.dakgg.io/api/v0/leaderboard/characters/${character}?seasonKey=${season}&teamMode=${teamMode}&sortType=${sortType}&page=${page}&hl=zh_CN"
        }
    }


    object Data {
        fun weaponV1API() = RequestDetailed().apply {
            url = "https://er.dakgg.io/api/v1/data/masteries?hl=zh_cn"
            method = "GET"
        }

        fun seasonV1API() = RequestDetailed().apply {
            url = "https://er.dakgg.io/api/v1/data/seasons?hl=zh_cn"
            method = "GET"
        }

        fun traitSkillsV1API() = RequestDetailed().apply {
            url = "https://er.dakgg.io/api/v1/data/trait-skills?hl=zh-cn"
            method = "GET"
        }

        /**
         * 实验体信息，包含皮肤、属性、实验体图片
         */
        fun charactersV1API() = RequestDetailed().apply {
            url = "https://er.dakgg.io/api/v1/data/characters?hl=zh_cn"
            method = "GET"
        }

        /**
         * 段位信息
         */
        fun tiersV1API() = RequestDetailed().apply {
            url = "https://er.dakgg.io/api/v1/data/tiers?hl=zh_cn"
            method = "GET"
        }

    }

    object Statistics {
        fun tierDistributionV0API(teamMode: String = "SQUAD") = RequestDetailed().apply {
            url = "https://er.dakgg.io/api/v0/statistics/tier-distribution?teamMode=${teamMode}&hl=zh_CN"
            method = "GET"
        }
    }

    object Download {
        suspend fun dakGGDownloadURIStreamFile(uri: String, outputPath: String) {
            val resp = HTTPRequest.requestRetry {
                it.url = "https://cdn.dak.gg${uri}"
                it.method = "get"
            }
            resp?.let { ReadWriteFile.writeStreamFile(outputPath, resp.bodyStream()) }
        }

        /**
         * 获取段位图标 round
         * iconUrl
         * @return image path
         */
        suspend fun dakGGDownloadTierIcon(id: Int): String {
            val eternalReturnDataImagePath = PathUtils.getEternalReturnDataImagePath("tier/${id}.png")
            if (!File(eternalReturnDataImagePath).exists()) {
                dakGGDownloadURIStreamFile(
                    "/er/images/tier/round/$id.png",
                    eternalReturnDataImagePath
                )
            }
            return eternalReturnDataImagePath
        }

        /**
         * 装备背景图片 表示装备品级
         * 持久化存储 应当缓存图片
         * @param id 需传递图片 数字id 00-06
         * @return 磁盘存储路径
         */
        suspend fun getItemGradeBg(id: Int): String {
            val eternalReturnDataImagePath = PathUtils.getEternalReturnDataImagePath("ico/itemgradebg-0${id}.svg")
            if (!File(eternalReturnDataImagePath).exists()) {
                // 写死 没关系 ^ ^
                dakGGDownloadURIStreamFile(
                    "/er/images/item/ico-itemgradebg-0${id}.svg",
                    eternalReturnDataImagePath
                )
            }
            return eternalReturnDataImagePath
        }


        /**
         *  下载文件(需要完整的uri)
         *  @param "//cdn.dak.gg/assets/er/game-assets/1.44.0/VSkillIcon_4103000.png"
         *
         */
        suspend fun downloadUrlStream(url: String, outputPath: String) {
            val resp: HttpResponse? =
                if (url.startsWith("http")) {
                    HTTPRequest.requestRetry {
                        it.url = url
                        it.method = "GET"
                    }
                } else {
                    HTTPRequest.requestRetry {
                        it.url = "https:${url}"
                        it.method = "get"
                    }
                }

            ReadWriteFile.writeStreamFile(outputPath, resp?.bodyStream())
        }
    }
}

