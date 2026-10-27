package cn.mykodb.graft.scratch

import cn.mykodb.graft.registry.ModDataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * 刮刮乐卡片。
 *
 * 右键打开刮刮乐界面；卡片本身只携带一个 `graft:card_variant` 组件，决定用哪套奖池与布局。
 * 物品标题显示对应变体的 `display_name`。
 */
class ScratchCardItem(properties: Properties) : Item(properties) {

    /** 读取卡片的变体 id，未设置时返回 null。 */
    fun variantId(stack: ItemStack) = stack.get(ModDataComponents.CARD_VARIANT)

    override fun getName(stack: ItemStack): Component {
        val variantId = variantId(stack) ?: return super.getName(stack)
        val displayName = ScratchCardTitles.displayName(variantId) ?: return super.getName(stack)
        return Component.translatable(displayName)
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)
        if (level.isClientSide) {
            return if (variantId(stack) != null) InteractionResult.SUCCESS else InteractionResult.FAIL
        }
        return if (ScratchCardOpener.open(player, stack)) {
            InteractionResult.CONSUME
        } else {
            InteractionResult.FAIL
        }
    }
}
