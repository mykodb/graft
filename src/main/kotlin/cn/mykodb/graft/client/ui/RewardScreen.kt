package cn.mykodb.graft.client.ui

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW

/**
 * 通用奖励展示界面。
 *
 * 它是一个独立的 [Screen]，配合 NeoForge 的 GUI 层栈
 * （[net.neoforged.neoforge.client.ClientHooks.pushGuiLayer]）压在当前界面之上：
 * 下层界面不会被移除，关闭奖品单后自动回到下层界面；也可以直接作为
 * [net.minecraft.client.Minecraft.setScreen] 的目标单独显示。
 *
 * 只依赖一组 [ItemStack]，与具体玩法无关，任何发奖流程都可以复用。
 */
class RewardScreen(rewards: List<ItemStack>) : Screen(Component.translatable("graft.reward.title")) {

    private val rewards: List<ItemStack> = rewards.filter { !it.isEmpty }.map { it.copy() }

    /**
     * 关闭时的回调；为 null 时走默认的 [Screen.onClose]（从 GUI 层栈弹出）。
     * 由宿主界面“叠加绘制”使用时，宿主设置该回调把覆盖层收起来即可。
     */
    var onDismiss: (() -> Unit)? = null

    override fun extractRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        // 整屏压暗，突出奖品面板。
        graphics.fill(0, 0, width, height, DIM_BACKGROUND)

        val columns = if (rewards.isEmpty()) 1 else minOf(MAX_COLUMNS, rewards.size)
        val rows = if (rewards.isEmpty()) 1 else (rewards.size + columns - 1) / columns
        // 面板至少容纳标题/提示文字，避免物品少时文字画到框外。
        val hint = Component.translatable("graft.reward.hint")
        val textWidth = maxOf(font.width(title), font.width(hint))
        val gridWidthPx = columns * CELL_WIDTH
        val panelWidth = maxOf(gridWidthPx + PANEL_PADDING * 2, textWidth + PANEL_PADDING * 2)
        val panelHeight = TITLE_HEIGHT + rows * CELL_HEIGHT + HINT_HEIGHT + PANEL_PADDING
        val panelX = (width - panelWidth) / 2
        val panelY = (height - panelHeight) / 2

        // 面板底色 + 一圈细边框。
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL_BACKGROUND)
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, PANEL_BORDER)
        graphics.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, PANEL_BORDER)
        graphics.fill(panelX, panelY, panelX + 1, panelY + panelHeight, PANEL_BORDER)
        graphics.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, PANEL_BORDER)

        // 标题
        graphics.centeredText(font, title, width / 2, panelY + 6, TEXT_COLOR)

        // 奖品：图标 + 数量
        val gridX = panelX + (panelWidth - gridWidthPx) / 2
        val gridY = panelY + TITLE_HEIGHT + 4
        for ((index, stack) in rewards.withIndex()) {
            val column = index % columns
            val row = index / columns
            val cellX = gridX + column * CELL_WIDTH
            val cellY = gridY + row * CELL_HEIGHT
            val itemX = cellX + (CELL_WIDTH - 16) / 2
            graphics.item(stack, itemX, cellY)
            // 数量用原版物品装饰绘制：右下角数字 + 阴影，跟背包/箱子里的样式一致。
            graphics.itemDecorations(font, stack, itemX, cellY)
        }
        if (rewards.isEmpty()) {
            graphics.centeredText(
                font,
                Component.translatable("graft.reward.none"),
                width / 2,
                gridY + 6,
                TEXT_COLOR,
            )
        }

        // 底部提示
        graphics.centeredText(
            font,
            hint,
            width / 2,
            panelY + panelHeight - HINT_HEIGHT - 2,
            HINT_COLOR,
        )
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        onClose()
        return true
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.key() == GLFW.GLFW_KEY_SPACE || event.key() == GLFW.GLFW_KEY_ESCAPE) {
            dismiss()
            return true
        }
        return super.keyPressed(event)
    }

    override fun onClose() {
        val dismiss = onDismiss
        if (dismiss != null) dismiss() else super.onClose()
    }

    private fun dismiss() {
        onClose()
    }

    override fun isPauseScreen(): Boolean = false

    companion object {
        private const val MAX_COLUMNS = 4
        private const val PANEL_PADDING = 8
        private const val TITLE_HEIGHT = 16
        private const val HINT_HEIGHT = 12
        private const val CELL_WIDTH = 24
        private const val CELL_HEIGHT = 28
        private const val DIM_BACKGROUND = 0x99000000.toInt()
        private const val PANEL_BACKGROUND = 0xE0181818.toInt()
        private const val PANEL_BORDER = 0xFFE0E0E0.toInt()
        private const val TEXT_COLOR = 0xFFFFFFFF.toInt()
        private const val HINT_COLOR = 0xFFA0A0A0.toInt()
    }
}
