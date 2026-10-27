package cn.mykodb.graft.datagen

import cn.mykodb.graft.Graft
import cn.mykodb.graft.registry.ModItems
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.ModelProvider
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.data.PackOutput

/**
 * 客户端资源模型的数据生成。
 *
 * 26.1 起物品模型由 `assets/<ns>/items/<id>.json` 定义，旧的 `models/item/<id>.json`
 * 只是被引用的模型文件。`generateFlatItem` 会同时写这两个文件。
 *
 * 这里只覆盖 [registerModels]、不调用父类默认的 `run()`，避免把原版全部物品再生成一遍。
 * 之后新增物品/方块时在这里补对应生成调用即可。
 */
class GraftModelProvider(output: PackOutput) : ModelProvider(output, Graft.MOD_ID) {
    override fun registerModels(blockModels: BlockModelGenerators, itemModels: ItemModelGenerators) {
        itemModels.generateFlatItem(ModItems.SCRATCH_CARD.get(), ModelTemplates.FLAT_ITEM)
    }
}
