package cn.mykodb.graft.datagen

import cn.mykodb.graft.Graft
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.data.event.GatherDataEvent

/**
 * 数据生成入口，由 `./gradlew runData` 触发。
 *
 * 只注册在客户端数据生成环境（clientData），普通游戏启动不会走到这里。
 */
@EventBusSubscriber(modid = Graft.MOD_ID, value = [Dist.CLIENT])
object GraftDataGenerator {
    @SubscribeEvent
    fun gatherData(event: GatherDataEvent.Client) {
        val output = event.generator.packOutput
        val lookup = event.lookupProvider
        event.addProvider(GraftModelProvider(output))
        event.addProvider(GraftLanguageProvider(output, "en_us"))
        event.addProvider(GraftLanguageProvider(output, "zh_cn"))
        event.addProvider(GraftDatapackProvider.create(output, lookup))
    }
}
