package cn.mykodb.graft.datagen

import cn.mykodb.graft.Graft
import cn.mykodb.graft.scratch.ScratchCardRegistries
import cn.mykodb.graft.scratch.ScratchCardVariant
import cn.mykodb.graft.scratch.ScratchLayout
import cn.mykodb.graft.scratch.ScratchPrizeSet
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.data.PackOutput
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider
import java.util.concurrent.CompletableFuture

/**
 * 三个刮刮乐数据包注册表的数据生成：变体 / 布局 / 奖池集。
 *
 * 输出 `data/graft/graft/<registry_path>/<name>.json`，与手写数据文件同一套路径规则。
 * 新增变体（比如“稀有刮刮乐”）时在这里补一条 register 即可。
 */
object GraftDatapackProvider {
    private fun id(name: String): Identifier = Identifier.fromNamespaceAndPath(Graft.MOD_ID, name)

    private fun <T : Any> key(registry: ResourceKey<Registry<T>>, name: String): ResourceKey<T> =
        ResourceKey.create(registry, id(name))

    private val BUILDER: RegistrySetBuilder = RegistrySetBuilder().apply {
        add<ScratchCardVariant>(ScratchCardRegistries.VARIANT) { ctx ->
            ctx.register(
                key(ScratchCardRegistries.VARIANT, "common"),
                ScratchCardVariant(
                    displayName = "graft.scratch_card.common",
                    prizeSet = id("common"),
                    layout = id("common"),
                    winChance = 0.3f,
                ),
            )
        }
        add<ScratchLayout>(ScratchCardRegistries.LAYOUT) { ctx ->
            ctx.register(
                key(ScratchCardRegistries.LAYOUT, "common"),
                ScratchLayout(
                    columns = 5,
                    cells = 15,
                    cellWidth = 18,
                    cellHeight = 18,
                    title = "graft.scratch_card.common",
                ),
            )
        }
        add<ScratchPrizeSet>(ScratchCardRegistries.PRIZE_SET) { ctx ->
            ctx.register(
                key(ScratchCardRegistries.PRIZE_SET, "common"),
                ScratchPrizeSet(VANILLA_POOLS.map { Identifier.fromNamespaceAndPath("minecraft", it) }),
            )
        }
    }

    /**
     * 卡池：三个档位的原版箱子战利品表，每格随机挑一张再抽一件奖品。
     *
     * 想换卡池就改这个列表；只留一张表表示所有格子共用同一张表。
     */
    private val VANILLA_POOLS: List<String> = listOf(
        "chests/simple_dungeon",
        "chests/desert_pyramid",
        "chests/ancient_city",
    )

    fun create(
        output: PackOutput,
        lookup: CompletableFuture<HolderLookup.Provider>,
    ): DatapackBuiltinEntriesProvider =
        DatapackBuiltinEntriesProvider(output, lookup, BUILDER, setOf(Graft.MOD_ID))
}
