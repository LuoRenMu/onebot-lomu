package action.commandProcess.eternalReturn.entity

import kotlinx.serialization.Serializable

/**
 * @author LoMu
 * Date 2024.07.31 9:00
 */
@Serializable
data class EternalReturnCharacterById(
    val id: Int = 0,
    val key: String = "",
    val name: String = "",
    val imageName: String = "",
    val imageUrl: String = "",
    val communityImageUrl: String = "",
    val weaponTypes: List<WeaponType> = arrayListOf(),
    val skins: List<Skin> = arrayListOf(),
) {
    @Serializable
    data class Skin(
        val id: Long = 0,
        val name: String = "",
        val grade: Int = 0,
        val imageName: String = "",
        val imageUrl: String = "",

        )

    @Serializable
    data class WeaponType(
        val id: Long = 0,
        val key: String = "",
    )

    enum class CharacterImgUrlType(val type: String) {
        ResultImageUrl("ResultImage"),
        CommunityImageUrl("CommunityImage"),
        ImageUrl("Image"),
        CharProfileImageUrl("CharProfileImage")
    }
}
