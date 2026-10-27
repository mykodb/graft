package cn.mykodb.graft.scratch

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.Identifier

/**
 * 一张刮刮乐的“变体”：决定这张卡用哪套卡池、哪套布局、中奖率多少。
 *
 * 数据文件位于 `data/<datapack_ns>/graft/scratch_card_variant/<name>.json`。
 */
data class ScratchCardVariant(
    /** 界面上显示的名称，支持翻译键（如 `graft.scratch_card.common`）。 */
    val displayName: String,
    /** 指向 `graft:scratch_prize_set` 注册表中的卡池。 */
    val prizeSet: Identifier,
    /** 指向 `graft:scratch_layout` 注册表中的布局。 */
    val layout: Identifier,
    /** 每个格子的中奖概率（0~1）：奖品抽出来之后，再按这个概率判定能不能领。 */
    val winChance: Float = DEFAULT_WIN_CHANCE,
) {
    companion object {
        const val DEFAULT_WIN_CHANCE: Float = 0.5f

        val CODEC: Codec<ScratchCardVariant> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("display_name").forGetter(ScratchCardVariant::displayName),
                Identifier.CODEC.fieldOf("prize_set").forGetter(ScratchCardVariant::prizeSet),
                Identifier.CODEC.fieldOf("layout").forGetter(ScratchCardVariant::layout),
                Codec.floatRange(0f, 1f)
                    .fieldOf("win_chance")
                    .forGetter(ScratchCardVariant::winChance),
            ).apply(instance, ::ScratchCardVariant)
        }
    }
}
