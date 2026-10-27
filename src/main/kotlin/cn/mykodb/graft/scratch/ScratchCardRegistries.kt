package cn.mykodb.graft.scratch

import cn.mykodb.graft.Graft
import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DataPackRegistryEvent

/**
 * 刮刮乐的三个数据包注册表。
 *
 * 用 datapack registry 而不是自建 reload listener 的原因：
 * - 支持数据包条件（`neoforge:conditions`）；
 * - 网络编码器非空时会自动把条目同步给客户端，客户端界面直接能查布局/变体；
 * - 路径、重复检测、`/reload` 全部由原版机制处理。
 *
 * 数据文件路径为 `data/<datapack_ns>/graft/<registry_path>/<name>.json`。
 */
object ScratchCardRegistries {
    val VARIANT: ResourceKey<Registry<ScratchCardVariant>> = registryKey("scratch_card_variant")
    val PRIZE_SET: ResourceKey<Registry<ScratchPrizeSet>> = registryKey("scratch_prize_set")
    val LAYOUT: ResourceKey<Registry<ScratchLayout>> = registryKey("scratch_layout")

    private fun <T : Any> registryKey(path: String): ResourceKey<Registry<T>> =
        ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(Graft.MOD_ID, path))

    /** 在模组事件总线上注册三个数据包注册表（服务端 + 客户端都要执行）。 */
    fun register(bus: IEventBus) {
        bus.addListener(DataPackRegistryEvent.NewRegistry::class.java) { event ->
            event.dataPackRegistry(VARIANT, ScratchCardVariant.CODEC, ScratchCardVariant.CODEC)
            event.dataPackRegistry(PRIZE_SET, ScratchPrizeSet.CODEC, ScratchPrizeSet.CODEC)
            event.dataPackRegistry(LAYOUT, ScratchLayout.CODEC, ScratchLayout.CODEC)
        }
    }
}

private fun <T : Any> RegistryAccess.getOrNull(key: ResourceKey<Registry<T>>, id: Identifier): T? =
    lookup(key).orElse(null)?.getValue(ResourceKey.create(key, id))

fun RegistryAccess.scratchVariant(id: Identifier): ScratchCardVariant? = getOrNull(ScratchCardRegistries.VARIANT, id)

fun RegistryAccess.scratchPrizeSet(id: Identifier): ScratchPrizeSet? = getOrNull(ScratchCardRegistries.PRIZE_SET, id)

fun RegistryAccess.scratchLayout(id: Identifier): ScratchLayout? = getOrNull(ScratchCardRegistries.LAYOUT, id)
