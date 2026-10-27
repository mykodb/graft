package cn.mykodb.graft.scratch

import cn.mykodb.graft.Graft
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier

/**
 * 客户端 -> 服务端：请求提前结算当前刮刮乐。
 *
 * 玩家刮开达到阈值时立即发送，服务端据此发奖并提示，而不是等到关闭界面。
 */
data class ScratchCardClaimPayload(val containerId: Int) : CustomPacketPayload {

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<ScratchCardClaimPayload> =
            CustomPacketPayload.Type(Identifier.fromNamespaceAndPath(Graft.MOD_ID, "scratch_card_claim"))

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ScratchCardClaimPayload> =
            StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                ScratchCardClaimPayload::containerId,
                ::ScratchCardClaimPayload,
            )
    }
}
