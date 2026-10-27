package cn.mykodb.graft.client

import cn.mykodb.graft.Graft
import cn.mykodb.graft.registry.ModMenus
import cn.mykodb.graft.scratch.ScratchCardTitles
import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent

// RegisterMenuScreensEvent 继承 IModBusEvent，会自动注册到 mod 事件总线。
// Kotlin object 会被按“实例”注册，因此处理方法不能是 static（不要加 @JvmStatic）。
@EventBusSubscriber(modid = Graft.MOD_ID, value = [Dist.CLIENT])
object ClientEvents {

    @SubscribeEvent
    fun onRegisterMenuScreens(event: RegisterMenuScreensEvent) {
        event.register(ModMenus.SCRATCH_CARD.get()) { menu, inventory, title ->
            ScratchCardScreen(menu, inventory, title)
        }
    }

    /** 把客户端当前 level 的注册表快照交给物品标题解析（纯客户端连远程服时用）。 */
    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent.Post) {
        ScratchCardTitles.updateClientRegistryAccess(Minecraft.getInstance().level?.registryAccess())
    }
}
