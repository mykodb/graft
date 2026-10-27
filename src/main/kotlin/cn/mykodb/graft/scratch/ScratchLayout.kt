package cn.mykodb.graft.scratch

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

/**
 * 布局：决定刮刮乐界面的行列数与单格大小（“大小样式”）。
 *
 * 数据文件位于 `data/<datapack_ns>/graft/scratch_layout/<name>.json`。
 */
data class ScratchLayout(
    /** 每行列数。 */
    val columns: Int,
    /** 格子总数，行数 = ceil(cells / columns)。 */
    val cells: Int,
    /** 单格宽度（像素）。 */
    val cellWidth: Int,
    /** 单格高度（像素）。 */
    val cellHeight: Int,
    /** 界面标题，支持翻译键。 */
    val title: String,
) {
    companion object {
        val CODEC: Codec<ScratchLayout> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.intRange(1, 64).fieldOf("columns").forGetter(ScratchLayout::columns),
                Codec.intRange(1, 256).fieldOf("cells").forGetter(ScratchLayout::cells),
                Codec.intRange(1, 256).fieldOf("cell_width").forGetter(ScratchLayout::cellWidth),
                Codec.intRange(1, 256).fieldOf("cell_height").forGetter(ScratchLayout::cellHeight),
                Codec.STRING.fieldOf("title").forGetter(ScratchLayout::title),
            ).apply(instance, ::ScratchLayout)
        }
    }
}
