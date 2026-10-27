package cn.mykodb.graft.scratch

import net.minecraft.world.item.ItemStack

/**
 * 一个格子的结算结果：先抽到的奖品 + 是否中奖。
 *
 * 参考异环式刮刮乐：格子里总会摆出奖品，但只有带中奖标记的格子才能领取；
 * 没中奖的格子只是展示，领奖时会被忽略。
 */
data class ScratchCell(
    /** 抽到的奖品；`ItemStack.EMPTY` 表示这一格抽空了（此时必然不中奖）。 */
    val stack: ItemStack,
    /** 是否中奖。 */
    val won: Boolean,
)
