package cn.mykodb.graft.datagen

import cn.mykodb.graft.Graft
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider

/**
 * 语言文件的数据生成。每加一个语言就多注册一个实例。
 *
 * 键值集中放在下面两张表里，改文案只动这里。
 */
class GraftLanguageProvider(
    output: PackOutput,
    private val locale: String,
) : LanguageProvider(output, Graft.MOD_ID, locale) {
    override fun addTranslations() {
        val translations = if (locale == "zh_cn") ZH_CN else EN_US
        translations.forEach { (key, value) -> add(key, value) }
    }

    private companion object {
        val EN_US: Map<String, String> = linkedMapOf(
            "item.graft.scratch_card" to "Scratch Card",
            "itemGroup.graft" to "Graft",
            "graft.scratch_card.common" to "Common Scratch Card",
            "graft.scratch_card.hint" to "Press Space to reveal all",
            "graft.scratch_card.close_hint" to "Click to close",
            "graft.reward.title" to "Rewards",
            "graft.reward.hint" to "Click or press Space to close",
            "graft.reward.none" to "No reward",
        )

        val ZH_CN: Map<String, String> = linkedMapOf(
            "item.graft.scratch_card" to "刮刮乐",
            "itemGroup.graft" to "Graft",
            "graft.scratch_card.common" to "普通刮刮乐",
            "graft.scratch_card.hint" to "空格一键刮开",
            "graft.scratch_card.close_hint" to "点击关闭",
            "graft.reward.title" to "获得奖励",
            "graft.reward.hint" to "点击或按空格关闭",
            "graft.reward.none" to "没有中奖",
        )
    }
}
