package cn.mykodb.graft.scratch

import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams

/**
 * 刮刮乐抽奖规则：异环式两段抽取。
 *
 * 每个格子分两步：
 *  1. 从卡池里随机挑一张战利品表，抽出一件奖品，摆在格子里；
 *  2. 再按变体的 [ScratchCardVariant.winChance] 独立判定这一格是否中奖。
 *
 * 只有中奖的格子能领取，没中奖的格子只做展示。
 * 抽到什么、中不中奖都完全由服务端决定，客户端只拿 [ScratchCell] 做显示。
 */
object ScratchCardRules {

    /** 混入格子下标，保证同一个 seed 下每格抽到的结果不同。 */
    private const val SLOT_SALT = 0x9E3779B97F4A7C15uL

    /**
     * 抽取一格。
     *
     * @return 抽到的奖品与中奖标记；奖品为空时一定 `won = false`。
     */
    fun drawSlot(
        level: ServerLevel,
        player: Player,
        prizeSet: ScratchPrizeSet,
        winChance: Float,
        seed: Long,
    ): ScratchCell {
        val random = RandomSource.create(seed)
        val pools = prizeSet.pools
        val tableId = when {
            pools.isEmpty() -> null
            pools.size == 1 -> pools.first()
            else -> pools[random.nextInt(pools.size)]
        } ?: return ScratchCell(ItemStack.EMPTY, false)

        val stack = drawFromTable(level, player, tableId, random.nextLong()) ?: ItemStack.EMPTY
        // 表里什么都没抽出来时，这一格不算中奖。
        val won = !stack.isEmpty && random.nextFloat() < winChance
        return ScratchCell(stack, won)
    }

    /** 按卡池抽满 [cells] 个格子。 */
    fun drawAll(
        level: ServerLevel,
        player: Player,
        prizeSet: ScratchPrizeSet,
        cells: Int,
        winChance: Float,
        seed: Long,
    ): List<ScratchCell> = List(cells) { index ->
        drawSlot(level, player, prizeSet, winChance, mix(seed, index))
    }

    /** 从单张战利品表抽一件奖品；表抽空时返回 `null`。 */
    private fun drawFromTable(level: ServerLevel, player: Player, tableId: Identifier, seed: Long): ItemStack? {
        val table: LootTable = level.server.reloadableRegistries()
            .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, tableId))

        val params = LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, player.position())
            .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
            .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player)
            .create(LootContextParamSets.CHEST)

        return table.getRandomItems(params, RandomSource.create(seed)).firstOrNull()
    }

    private fun mix(seed: Long, slot: Int): Long = seed xor (slot * SLOT_SALT.toLong())
}
