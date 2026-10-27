package cn.mykodb.graft.scratch

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.Identifier

/**
 * 卡池：每格抽奖时，先从 [pools] 里随机挑一张战利品表，再从那张表里抽一件奖品。
 *
 * 这样奖池只维护“奖品来源”几张表，配几个格子、多少格子中奖都不用在这里管；
 * 中奖与否由 [ScratchCardVariant.winChance] 单独判定。
 *
 * 数组长度可以任意：1 张表示所有格子共用同一张表，多张表示每格随机抽到不同档位的表。
 * 数据文件位于 `data/<datapack_ns>/graft/scratch_prize_set/<name>.json`。
 */
data class ScratchPrizeSet(
    /** 卡池候选战利品表 id，例如 `minecraft:chests/simple_dungeon`。 */
    val pools: List<Identifier>,
) {
    companion object {
        val CODEC: Codec<ScratchPrizeSet> = RecordCodecBuilder.create { instance ->
            instance.group(
                Identifier.CODEC.listOf().fieldOf("pools").forGetter(ScratchPrizeSet::pools),
            ).apply(instance, ::ScratchPrizeSet)
        }
    }
}
