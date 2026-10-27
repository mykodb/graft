package cn.mykodb.graft.registry

import cn.mykodb.graft.Graft
import cn.mykodb.graft.registry.ModItems.ITEMS
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

object CreativeTabs {
    val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Graft.MOD_ID)

    val GRAFT_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> =
        CREATIVE_MODE_TABS.register("graft") { ->
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.graft"))
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon { ModItems.SCRATCH_CARD.toStack() }
                .displayItems { _, output ->
                    ITEMS.entries.forEach { output.accept(it.get().defaultInstance) }
                }.build()
        }
}
