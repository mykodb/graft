package cn.mykodb.graft.scratch

import net.minecraft.core.RegistryAccess
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.server.ServerLifecycleHooks

/**
 * 解析变体（variant）的显示名，供卡片物品的标题使用。
 *
 * 单人/服务端优先用当前服务器的注册表；纯客户端（连远程服）用客户端每 tick 同步的注册表快照，
 * 这样 `/reload` 之后物品标题也能跟着更新。
 */
object ScratchCardTitles {

    @Volatile
    private var clientRegistryAccess: RegistryAccess? = null

    /** 客户端每 tick 由 [cn.mykodb.graft.client.ClientEvents] 调用，保持注册表快照最新。 */
    fun updateClientRegistryAccess(access: RegistryAccess?) {
        clientRegistryAccess = access
    }

    /** 返回变体的 `display_name`；注册表或变体不存在时返回 null。 */
    fun displayName(variantId: Identifier): String? {
        val access = ServerLifecycleHooks.getCurrentServer()?.registryAccess() ?: clientRegistryAccess
        return access?.scratchVariant(variantId)?.displayName
    }
}
