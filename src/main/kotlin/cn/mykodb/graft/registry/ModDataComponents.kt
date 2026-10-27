package cn.mykodb.graft.registry

import cn.mykodb.graft.Graft
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

object ModDataComponents {
    val COMPONENTS: DeferredRegister.DataComponents =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Graft.MOD_ID)

    /** 卡片使用的变体 id（`graft:scratch_card_variant` 注册表条目）。 */
    val CARD_VARIANT: DeferredHolder<DataComponentType<*>, DataComponentType<Identifier>> =
        COMPONENTS.registerComponentType("card_variant") { builder ->
            builder
                .persistent(Identifier.CODEC)
                .networkSynchronized(Identifier.STREAM_CODEC)
        }
}
