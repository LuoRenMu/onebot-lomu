package cn.luorenmu.service

import action.commandProcess.eternalReturn.entity.EternalReturnCharacterById
import cn.luorenmu.action.request.EternalReturnRequestData
import cn.luorenmu.action.request.api.EternalReturnDakGGAPI
import cn.luorenmu.common.utils.StringLockUtils
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

/**
 * @author LoMu
 * Date 2025.04.18 12:35
 */
@Service
class ImageService(
    private val eternalReturnRequestData: EternalReturnRequestData,
) {

    suspend fun getTierImage(id: Int) =
        EternalReturnDakGGAPI.Download.dakGGDownloadTierIcon(id)

    suspend fun getEternalReturnCharacterImage(
        type: EternalReturnCharacterById.CharacterImgUrlType,
        id: Int,
        skin: Long,
    ) = StringLockUtils.lock("${type.type}-$skin:$id") {
        eternalReturnRequestData.getCharacterImg(
            id, type, skin
        )
    }

    suspend fun getEternalReturnItemImage(id: Long) =
        StringLockUtils.lock("item:$id") {
            eternalReturnRequestData.getItemIcon(id)
        }


    suspend fun getEternalReturnTacticalSkillImage(id: Long) = StringLockUtils.lock("tactical_skill:$id") {
        eternalReturnRequestData.getTacticalSkillIcon(id)
    }


    suspend fun getEternalReturnWeaponImage(id: Int) = StringLockUtils.lock("weapon:$id") {
        eternalReturnRequestData.getWeaponIcon(id)
    }


    suspend fun getEternalReturnItemBgImage(id: Int) =
        StringLockUtils.lock("itemBg:$id") {
            EternalReturnDakGGAPI.Download.getItemGradeBg(id)

        }


    suspend fun getEternalReturnTraitSkillImage(id: Long) =
        StringLockUtils.lock("TraitSkill:$id") {
            eternalReturnRequestData.getTraitSkillsIcon(id)
        }


    suspend fun getCharacterImgUrl(
        type: EternalReturnCharacterById.CharacterImgUrlType,
        id: Int,
        skin: Long = -1,
    ) = coroutineScope {
        getEternalReturnCharacterImage(type, id, skin)
        "/images/eternal_return/character/$type/$id/$skin"
    }


    suspend fun getItemImgUrl(id: Long) = run {
        getEternalReturnItemImage(id)
        "/images/eternal_return/item/${id}"
    }

    suspend fun getTierImgUrl(id: Int) = run {
        getTierImage(id)
        "/images/eternal_return/tier/${id}"
    }

    suspend fun getItemImgBgUrl(id: Int) = run {
        getEternalReturnItemBgImage(id)
        "/images/eternal_return/item_bg/${id}"
    }

    suspend fun getTraitSkillImgUrl(id: Long, `is`: Boolean = false) = run {
        getEternalReturnTraitSkillImage(id)
        "/images/eternal_return/trait_skill/${id}?is=${`is`}"
    }

    suspend fun getTacticalSkillImgUrl(id: Long) = run {
        getEternalReturnTacticalSkillImage(id)
        "/images/eternal_return/tactical_skill/${id}"
    }

    suspend fun getWeaponImgUrl(id: Int) = run {
        getEternalReturnWeaponImage(id)
        "/images/eternal_return/weapon/${id}"
    }
}