package cn.mykodb.graft.registry

import cn.mykodb.graft.Graft
import cn.mykodb.graft.scratch.ScratchCardMenu
import cn.mykodb.graft.scratch.ScratchCardView
import net.minecraft.core.registries.Registries
import net.minecraft.world.inventory.MenuType
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModMenus {
    val MENUS = DeferredRegister.create(Registries.MENU, Graft.MOD_ID)

    val SCRATCH_CARD: DeferredHolder<MenuType<*>, MenuType<ScratchCardMenu>> =
        MENUS.register("scratch_card", Supplier {
            IMenuTypeExtension.create { windowId, _, data ->
                ScratchCardMenu(windowId, ScratchCardView.read(data))
            }
        })
}
