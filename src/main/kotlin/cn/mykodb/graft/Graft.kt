package cn.mykodb.graft

import cn.mykodb.graft.config.Config
import cn.mykodb.graft.network.ModPayloads
import cn.mykodb.graft.registry.*
import cn.mykodb.graft.scratch.ScratchCardRegistries
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

@Mod(Graft.MOD_ID)
class Graft {
    constructor(modEventBus: IEventBus, modContainer: ModContainer) {
        ModBlocks.BLOCKS.register(modEventBus)
        // 物品默认组件引用了数据组件类型，必须保证组件类型先注册。
        ModDataComponents.COMPONENTS.register(modEventBus)
        ModItems.ITEMS.register(modEventBus)
        ModMenus.MENUS.register(modEventBus)
        CreativeTabs.CREATIVE_MODE_TABS.register(modEventBus)
        ScratchCardRegistries.register(modEventBus)
        modEventBus.addListener<RegisterPayloadHandlersEvent> { ModPayloads.register(it) }
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)
    }

    companion object {
        const val MOD_ID = "graft"
    }
}
