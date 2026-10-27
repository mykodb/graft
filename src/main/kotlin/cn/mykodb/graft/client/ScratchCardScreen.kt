package cn.mykodb.graft.client

import cn.mykodb.graft.client.ScratchCardScreen.Companion.REVEAL_THRESHOLD
import cn.mykodb.graft.client.ui.RewardScreen
import cn.mykodb.graft.scratch.ScratchCardClaimPayload
import cn.mykodb.graft.scratch.ScratchCardMenu
import cn.mykodb.graft.scratch.ScratchCardView
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.neoforged.neoforge.client.ClientHooks
import net.neoforged.neoforge.client.network.ClientPacketDistributor
import org.lwjgl.glfw.GLFW

/**
 * 刮刮乐界面：不使用任何槽位，格子内容按像素坐标直接渲染物品图标。
 *
 * 未刮开状态由一块与网格同尺寸的 [DynamicTexture] 覆盖，鼠标按住拖动时把经过的像素
 * 的 alpha 置 0 并重新上传纹理，从而“刮开”看到下面的物品。
 *
 * 刮开比例达到 [REVEAL_THRESHOLD] 时立即向服务端请求发奖，并把独立的 [RewardScreen]
 * 压到 GUI 层栈顶做提醒；关闭奖品单后回到本界面。如果没开奖就直接关闭，则关闭后再把
 * 奖品单弹出来提醒。
 */
class ScratchCardScreen(
    cardMenu: ScratchCardMenu,
    inventory: Inventory,
    title: Component,
) : AbstractContainerScreen<ScratchCardMenu>(
    cardMenu,
    inventory,
    title,
    viewWidth(cardMenu.view),
    viewHeight(cardMenu.view),
) {

    private var maskImage: NativeImage? = null
    private var maskTexture: DynamicTexture? = null
    private var maskDirty = false
    private var maskWidth = 0
    private var maskHeight = 0
    private var scratching = false
    private var totalPixels = 0
    private var erasedPixels = 0
    private var claimed = false

    override fun extractRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        val view = getMenu().view
        val x = leftPos
        val y = topPos

        // 背板
        graphics.fill(x, y, x + imageWidth, y + imageHeight, BACKGROUND)
        // 标题（相对面板居中）
        graphics.centeredText(font, title, x + imageWidth / 2, y + TITLE_PAD, TITLE_COLOR)

        // 格子内容（底 + 物品），最后统一盖未刮开遮罩；网格在面板内水平居中。
        val gridX = x + (imageWidth - gridWidth(view)) / 2
        for (index in view.results.indices) {
            val col = index % view.columns
            val row = index / view.columns
            val cellX = gridX + col * (view.cellWidth + GAP)
            val cellY = y + TITLE_HEIGHT + row * (view.cellHeight + GAP)
            drawCell(graphics, view, index, cellX, cellY)
        }
        drawScratchMask(graphics, view)

        // 底部操作提示：居中绘制，超宽时截断，避免文字画到面板框外。
        val hint = if (claimed) CLOSE_HINT else HINT
        val hintText = font.plainSubstrByWidth(hint.string, imageWidth - PAD * 2)
        graphics.centeredText(font, hintText, x + imageWidth / 2, y + imageHeight - HINT_HEIGHT, HINT_COLOR)
    }

    private fun drawCell(
        graphics: GuiGraphicsExtractor,
        view: ScratchCardView,
        index: Int,
        cellX: Int,
        cellY: Int,
    ) {
        val cell = view.results[index]
        val right = cellX + view.cellWidth
        val bottom = cellY + view.cellHeight

        graphics.fill(cellX, cellY, right, bottom, CELL_BACKGROUND)

        // 格子内容：奖品图标（中奖与否都先摆出来，参考异环每格都有奖项）。
        val stack = cell.stack
        if (!stack.isEmpty) {
            val itemX = cellX + (view.cellWidth - 16) / 2
            val itemY = cellY + (view.cellHeight - 16) / 2
            graphics.item(stack, itemX, itemY)
        }

        if (cell.won) {
            // 中奖：1px 绿色描边。
            graphics.fill(cellX, cellY, right, cellY + 1, WIN_MARK_COLOR)
            graphics.fill(cellX, bottom - 1, right, bottom, WIN_MARK_COLOR)
            graphics.fill(cellX, cellY + 1, cellX + 1, bottom - 1, WIN_MARK_COLOR)
            graphics.fill(right - 1, cellY + 1, right, bottom - 1, WIN_MARK_COLOR)
        } else if (!stack.isEmpty) {
            // 未中奖：压一层暗色，表示这格只是展示、不能领。
            graphics.fill(cellX, cellY, right, bottom, LOSE_OVERLAY)
        }
    }

    /** 把整块刮刮层纹理画在网格区域上；有擦除改动时先重新上传。 */
    private fun drawScratchMask(graphics: GuiGraphicsExtractor, view: ScratchCardView) {
        val gridW = gridWidth(view)
        val gridH = gridHeight(view)
        if (gridW <= 0 || gridH <= 0) return
        val texture = ensureMask(gridW, gridH)
        if (maskDirty) {
            texture.upload()
            maskDirty = false
        }
        val gridX = leftPos + (imageWidth - gridW) / 2
        val gridY = topPos + TITLE_HEIGHT
        graphics.blit(
            texture.textureView,
            texture.sampler,
            gridX, gridY,
            gridX + gridW, gridY + gridH,
            0f, 1f, 0f, 1f,
        )
    }

    private fun ensureMask(width: Int, height: Int): DynamicTexture {
        val existing = maskTexture
        if (existing != null && maskWidth == width && maskHeight == height) return existing

        releaseMask()
        val image = NativeImage(width, height, true)
        for (py in 0 until height) {
            for (px in 0 until width) {
                val light = ((px / 3) + (py / 3)) % 2 == 0
                image.setPixel(px, py, if (light) MASK_LIGHT else MASK_DARK)
            }
        }
        val texture = DynamicTexture({ "graft_scratch_mask" }, image)
        maskImage = image
        maskTexture = texture
        maskWidth = width
        maskHeight = height
        totalPixels = width * height
        erasedPixels = 0
        maskDirty = false
        return texture
    }

    /** 在 GUI 坐标处按圆形笔刷擦除遮罩像素。 */
    private fun eraseAt(guiX: Double, guiY: Double) {
        val image = maskImage ?: return
        val centerX = (guiX - (leftPos + (imageWidth - maskWidth) / 2)).toInt()
        val centerY = (guiY - (topPos + TITLE_HEIGHT)).toInt()
        if (centerX < -BRUSH_RADIUS || centerY < -BRUSH_RADIUS ||
            centerX > maskWidth + BRUSH_RADIUS || centerY > maskHeight + BRUSH_RADIUS
        ) {
            return
        }

        var changed = 0
        val radiusSquared = BRUSH_RADIUS * BRUSH_RADIUS
        for (dy in -BRUSH_RADIUS..BRUSH_RADIUS) {
            val py = centerY + dy
            if (py < 0 || py >= maskHeight) continue
            for (dx in -BRUSH_RADIUS..BRUSH_RADIUS) {
                if (dx * dx + dy * dy > radiusSquared) continue
                val px = centerX + dx
                if (px < 0 || px >= maskWidth) continue
                if ((image.getPixel(px, py) ushr 24) != 0) {
                    image.setPixel(px, py, 0)
                    changed++
                }
            }
        }
        if (changed > 0) {
            erasedPixels += changed
            maskDirty = true
            // 结算放在松开鼠标时做，避免拖动过程中被奖励界面打断。
        }
    }

    private fun isInsideGrid(guiX: Double, guiY: Double): Boolean {
        val view = getMenu().view
        val gridX = leftPos + (imageWidth - gridWidth(view)) / 2
        val gridY = topPos + TITLE_HEIGHT
        return guiX >= gridX && guiY >= gridY &&
            guiX < gridX + gridWidth(view) && guiY < gridY + gridHeight(view)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        // 已开奖后，点击关闭界面。
        if (claimed) {
            onClose()
            return true
        }
        if (event.button() == 0 && isInsideGrid(event.x(), event.y())) {
            scratching = true
            eraseAt(event.x(), event.y())
            return true
        }
        return super.mouseClicked(event, doubleClick)
    }

    override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
        if (scratching) {
            eraseAt(event.x(), event.y())
            return true
        }
        return super.mouseDragged(event, dragX, dragY)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (scratching) {
            scratching = false
            // 松开鼠标时再判定：达到阈值就全部揭开并结算，避免拖拽中途弹奖励界面打断手感。
            if (isRevealedEnough()) {
                revealAll()
            }
            return true
        }
        return super.mouseReleased(event)
    }

    /** 刮开比例是否达到提前开奖阈值。 */
    private fun isRevealedEnough(): Boolean =
        totalPixels > 0 && erasedPixels.toFloat() / totalPixels >= REVEAL_THRESHOLD

    /**
     * 达到阈值：请求服务端发奖（发奖只发生在服务端），并把奖品单作为独立 Screen
     * 压在当前界面之上做提醒；刮刮乐界面保持打开。
     */
    private fun claimNow() {
        if (claimed) return
        claimed = true
        ClientPacketDistributor.sendToServer(ScratchCardClaimPayload(getMenu().containerId))
        showRewardScreen()
    }

    /** 把奖品单压到 GUI 层栈顶（本界面保留在栈里），关闭后自动回到本界面。 */
    private fun showRewardScreen() {
        val minecraft = minecraft
        ClientHooks.pushGuiLayer(minecraft, RewardScreen(getMenu().view.wonResults))
    }

    /** 空格一键刮开：清空整块遮罩。 */
    private fun revealAll() {
        val view = getMenu().view
        val gridW = gridWidth(view)
        val gridH = gridHeight(view)
        if (gridW <= 0 || gridH <= 0) return
        ensureMask(gridW, gridH)
        val image = maskImage ?: return
        image.fillRect(0, 0, maskWidth, maskHeight, 0)
        erasedPixels = totalPixels
        maskDirty = true
        claimNow()
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        val confirm = event.key() == GLFW.GLFW_KEY_SPACE || event.key() == GLFW.GLFW_KEY_ESCAPE
        if (claimed) {
            if (confirm) {
                onClose()
                return true
            }
            return super.keyPressed(event)
        }
        if (event.key() == GLFW.GLFW_KEY_SPACE) {
            revealAll()
            return true
        }
        return super.keyPressed(event)
    }

    /**
     * 关闭界面：如果还没开奖，先结算发奖，关闭本界面后再把奖品单作为顶层界面弹出来，
     * 让玩家看到获得的物品。
     */
    override fun onClose() {
        if (!claimed) {
            claimed = true
            ClientPacketDistributor.sendToServer(ScratchCardClaimPayload(getMenu().containerId))
            super.onClose()
            showRewardScreen()
            return
        }
        super.onClose()
    }

    override fun removed() {
        scratching = false
        releaseMask()
        // 被动关闭（切维度、被其他界面顶替、服务端关闭容器）不会走 onClose。
        // 此时奖励已由服务端 removed 兜底发放，这里延迟一帧补弹奖励面板，让玩家看得到获得物。
        if (!claimed) {
            claimed = true
            scheduleRewardScreenAfterRemoval()
        }
        super.removed()
    }

    /** 被动关闭时本界面正在被移除，直接压栈会被立刻覆盖，因此延迟一帧再弹。 */
    private fun scheduleRewardScreenAfterRemoval() {
        val minecraft = minecraft
        minecraft.execute {
            val player = minecraft.player
            if (player == null || player.isDeadOrDying) return@execute
            ClientHooks.pushGuiLayer(minecraft, RewardScreen(getMenu().view.wonResults))
        }
    }

    private fun releaseMask() {
        maskTexture?.close()
        maskTexture = null
        maskImage = null
        maskWidth = 0
        maskHeight = 0
        maskDirty = false
    }

    companion object {
        private const val PAD = 8
        private const val GAP = 2
        private const val TITLE_HEIGHT = 14
        private const val TITLE_PAD = 4
        private const val HINT_HEIGHT = 11
        private const val BACKGROUND = 0xF0181818.toInt()
        private const val CELL_BACKGROUND = 0xFF2B2B2B.toInt()
        private const val WIN_MARK_COLOR = 0xFF22C55E.toInt()
        private const val LOSE_OVERLAY = 0x99000000.toInt()
        private const val TITLE_COLOR = 0xFFFFFFFF.toInt()
        private const val HINT_COLOR = 0xFFA0A0A0.toInt()
        private const val BRUSH_RADIUS = 5
        private const val MIN_PANEL_WIDTH = 120
        private const val REVEAL_THRESHOLD = 0.8f
        private const val MASK_LIGHT = 0xFF9AA0A6.toInt()
        private const val MASK_DARK = 0xFF8B9197.toInt()

        private val HINT = Component.translatable("graft.scratch_card.hint")
        private val CLOSE_HINT = Component.translatable("graft.scratch_card.close_hint")

        private fun gridWidth(view: ScratchCardView) = view.columns * (view.cellWidth + GAP) - GAP

        private fun gridHeight(view: ScratchCardView): Int {
            if (view.columns <= 0 || view.results.isEmpty()) return 0
            val rows = (view.results.size + view.columns - 1) / view.columns
            return rows * (view.cellHeight + GAP) - GAP
        }

        fun viewWidth(view: ScratchCardView) = maxOf(gridWidth(view) + PAD * 2, MIN_PANEL_WIDTH)
        fun viewHeight(view: ScratchCardView) = TITLE_HEIGHT + gridHeight(view) + HINT_HEIGHT + PAD
    }
}
