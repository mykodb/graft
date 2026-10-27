package cn.mykodb.graft.network

import cn.mykodb.graft.scratch.ScratchCardClaimPayload
import cn.mykodb.graft.scratch.ScratchCardMenu
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadContext

/** 模组自定义网络包注册。 */
object ModPayloads {

    fun register(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar("1")
        registrar.playToServer(
            ScratchCardClaimPayload.TYPE,
            ScratchCardClaimPayload.STREAM_CODEC,
        ) { payload, context -> onClaim(payload, context) }
    }

    /** 客户端刮开达到阈值后请求发奖。 */
    private fun onClaim(payload: ScratchCardClaimPayload, context: IPayloadContext) {
        context.enqueueWork {
            val player = context.player()
            val menu = player.containerMenu
            if (menu is ScratchCardMenu && menu.containerId == payload.containerId) {
                menu.claim(player)
            }
        }
    }
}
