package cn.mykodb.graft.scratch

import cn.mykodb.graft.registry.ModMenus
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * 刮刮乐容器。
 *
 * 没有任何槽位：物品图标由客户端屏幕按像素坐标直接渲染。
 * 发放时机有两种：客户端刮开达到阈值时通过 [claim] 提前发奖，或关闭界面时由 [removed]
 * 兜底发奖。两种入口共用 [claim]，用 [settled] 保证只发一次。
 * 只发放中奖格子里的奖品；没中奖的格子只做展示。
 * 奖励展示由客户端屏幕的奖励面板负责，不走聊天栏。
 *
 * @param dimension 打开界面时玩家所在维度（仅服务端提供）；玩家切换维度后 [stillValid] 会失败，
 *   服务端下一 tick 主动关闭容器并走 [removed] 兜底发奖，不依赖客户端关闭包。
 */
class ScratchCardMenu(
    containerId: Int,
    val view: ScratchCardView,
    private val dimension: ResourceKey<Level>? = null,
) : AbstractContainerMenu(ModMenus.SCRATCH_CARD.get(), containerId) {

    private var settled = false

    override fun stillValid(player: Player): Boolean {
        // 客户端侧不做维度校验：切维度时客户端由 handleRespawn 自己关闭容器，且客户端拿不到可靠的维度来源。
        if (player.level().isClientSide) return true
        val dimension = dimension ?: return player.isAlive
        return player.isAlive && player.level().dimension() == dimension
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack = ItemStack.EMPTY

    /** 发放全部中奖奖品；重复调用或客户端侧调用都直接返回。 */
    fun claim(player: Player) {
        if (player.level().isClientSide || settled) return
        settled = true

        // 发放时再 copy 一次：placeItemBackInInventory 会消耗传入的 stack（count 归零）。
        view.wonResults.forEach {
            player.inventory.placeItemBackInInventory(it.copy())
        }
    }

    override fun removed(player: Player) {
        // 关闭界面时兜底：若已经提前发过奖则不会重复发。
        claim(player)
    }
}
