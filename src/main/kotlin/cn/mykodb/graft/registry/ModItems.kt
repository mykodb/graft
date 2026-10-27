package cn.mykodb.graft.registry

import cn.mykodb.graft.Graft
import cn.mykodb.graft.scratch.ScratchCardItem
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems {
    val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(Graft.MOD_ID)

    /** 默认给一个 `common` 变体，直接在创造栏拿出来即可测试；数据包可另给其他变体。 */
    val SCRATCH_CARD = ITEMS.registerItem("scratch_card") { properties ->
        ScratchCardItem(
            properties.component(
                ModDataComponents.CARD_VARIANT,
                Identifier.fromNamespaceAndPath(Graft.MOD_ID, "common"),
            )
        )
    }
}
