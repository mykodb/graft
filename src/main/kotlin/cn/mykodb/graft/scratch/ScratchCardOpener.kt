package cn.mykodb.graft.scratch

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

/**
 * 服务端开卡流程：查数据 → 抽奖 → 构造视图 → 打开界面 → 扣卡。
 */
object ScratchCardOpener {

    /** @return 是否成功打开界面 */
    fun open(player: Player, stack: ItemStack): Boolean {
        if (player.level().isClientSide) return false
        val level = player.level() as? ServerLevel ?: return false
        val serverPlayer = player as? ServerPlayer ?: return false

        val variantId = (stack.item as? ScratchCardItem)?.variantId(stack) ?: return false
        val registries = level.registryAccess()
        val variant = registries.scratchVariant(variantId) ?: return false
        val prizeSet = registries.scratchPrizeSet(variant.prizeSet) ?: return false
        val layout = registries.scratchLayout(variant.layout) ?: return false

        val results = ScratchCardRules.drawAll(
            level = level,
            player = player,
            prizeSet = prizeSet,
            cells = layout.cells,
            winChance = variant.winChance,
            seed = level.random.nextLong(),
        )

        val view = ScratchCardView(
            variantId = variantId,
            titleKey = layout.title,
            columns = layout.columns,
            cellWidth = layout.cellWidth,
            cellHeight = layout.cellHeight,
            results = results,
        )

        val provider = object : MenuProvider {
            override fun getDisplayName(): Component = Component.translatable(view.titleKey)

            override fun createMenu(
                containerId: Int,
                inventory: Inventory,
                player: Player,
            ): AbstractContainerMenu = ScratchCardMenu(containerId, view, player.level().dimension())

            override fun writeClientSideData(menu: AbstractContainerMenu, buffer: RegistryFriendlyByteBuf) {
                view.write(buffer)
            }
        }

        val opened = serverPlayer.openMenu(provider)
        if (opened.isEmpty) return false

        // 打开成功才消耗卡片；创造模式不消耗。
        if (!player.hasInfiniteMaterials()) {
            stack.shrink(1)
        }
        return true
    }
}
