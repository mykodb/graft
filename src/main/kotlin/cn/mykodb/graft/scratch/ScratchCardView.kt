package cn.mykodb.graft.scratch

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack

/**
 * 一次开卡的服务端结果，随打开界面一起下发到客户端。
 *
 * 客户端只负责显示：格子数量、布局、每格抽到的奖品与中奖标记。
 */
data class ScratchCardView(
    val variantId: Identifier,
    val titleKey: String,
    val columns: Int,
    val cellWidth: Int,
    val cellHeight: Int,
    val results: List<ScratchCell>,
) {
    val rows: Int
        get() = if (columns <= 0) 0 else (results.size + columns - 1) / columns

    /** 真正中奖、可以领取的奖品。 */
    val wonResults: List<ItemStack>
        get() = results.filter { it.won && !it.stack.isEmpty }.map { it.stack }

    fun write(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(variantId)
        buffer.writeUtf(titleKey)
        buffer.writeVarInt(columns)
        buffer.writeVarInt(cellWidth)
        buffer.writeVarInt(cellHeight)
        buffer.writeVarInt(results.size)
        for (cell in results) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, cell.stack)
            buffer.writeBoolean(cell.won)
        }
    }

    companion object {
        fun read(buffer: RegistryFriendlyByteBuf): ScratchCardView {
            val variantId = buffer.readIdentifier()
            val titleKey = buffer.readUtf()
            val columns = buffer.readVarInt()
            val cellWidth = buffer.readVarInt()
            val cellHeight = buffer.readVarInt()
            val count = buffer.readVarInt()
            val results = List(count) {
                val stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)
                val won = buffer.readBoolean()
                ScratchCell(stack, won)
            }
            return ScratchCardView(
                variantId = variantId,
                titleKey = titleKey,
                columns = columns,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                results = results,
            )
        }
    }
}
