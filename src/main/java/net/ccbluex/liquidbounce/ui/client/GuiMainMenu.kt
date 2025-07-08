package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.ccbluex.liquidbounce.LiquidBounce.clientVersionText
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.fontmanager.GuiFontManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedBorderRect
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.sin
import kotlin.random.Random

class GuiMainMenu : AbstractScreen() {

    private var animationTime = 0f

    private val stars = List(100) {
        Star(
            Random.nextFloat() * 800f,
            Random.nextFloat() * 480f,
            Random.nextFloat() * 1.5f + 0.2f,
            Random.nextFloat() * 2f + 0.5f
        )
    }

    override fun initGui() {
        val defaultHeight = height / 4 + 48
        val baseCol1 = width / 2 - 100
        val baseCol2 = width / 2 + 2

        +GuiButton(100, baseCol1, defaultHeight + 24, 98, 20, "Alt Manager")
        +GuiButton(103, baseCol2, defaultHeight + 24, 98, 20, "Mods")
        +GuiButton(109, baseCol1, defaultHeight + 24 * 2, 98, 20, "Font Manager")
        +GuiButton(102, baseCol2, defaultHeight + 24 * 2, 98, 20, "Configuration")
        +GuiButton(101, baseCol1, defaultHeight + 24 * 3, 98, 20, "Status")
        +GuiButton(108, baseCol2, defaultHeight + 24 * 3, 98, 20, "Contributors")
        +GuiButton(1, baseCol1, defaultHeight, 98, 20, I18n.format("menu.singleplayer"))
        +GuiButton(2, baseCol2, defaultHeight, 98, 20, I18n.format("menu.multiplayer"))
        +GuiButton(0, baseCol1, defaultHeight + 24 * 4, 98, 20, I18n.format("menu.options"))
        +GuiButton(4, baseCol2, defaultHeight + 24 * 4, 98, 20, I18n.format("menu.quit"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        animationTime += 0.01f

        drawAnimatedBackground()
        drawStars()

        drawRoundedBorderRect(
            width / 2f - 115, height / 4f + 35, width / 2f + 115, height / 4f + 175,
            2f,
            Integer.MIN_VALUE,
            Integer.MIN_VALUE,
            3F
        )

        Fonts.fontBold180.drawCenteredString(CLIENT_NAME, width / 2F, height / 8F, Color.WHITE.rgb, true)
        Fonts.fontSemibold35.drawCenteredString(
            clientVersionText,
            width / 2F + 148,
            height / 8F + Fonts.fontSemibold35.fontHeight,
            0xffffff,
            true
        )

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            1 -> mc.displayGuiScreen(GuiSelectWorld(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            4 -> mc.shutdown()
            100 -> mc.displayGuiScreen(GuiAltManager(this))
            101 -> mc.displayGuiScreen(GuiServerStatus(this))
            102 -> mc.displayGuiScreen(GuiClientConfiguration(this))
            103 -> mc.displayGuiScreen(GuiModsMenu(this))
            108 -> mc.displayGuiScreen(GuiContributors(this))
            109 -> mc.displayGuiScreen(GuiFontManager(this))
        }
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
    }

    private fun drawAnimatedBackground() {
        val color1 = Color.getHSBColor((animationTime % 1), 0.6f, 0.5f)
        val color2 = Color.getHSBColor((animationTime + 0.25f) % 1, 0.6f, 0.5f)
        val color3 = Color.getHSBColor((animationTime + 0.5f) % 1, 0.6f, 0.5f)
        val color4 = Color.getHSBColor((animationTime + 0.75f) % 1, 0.6f, 0.5f)

        val widthF = width.toFloat()
        val heightF = height.toFloat()

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        GL11.glBegin(GL11.GL_QUADS)

        GL11.glColor3f(color1.red / 255f, color1.green / 255f, color1.blue / 255f)
        GL11.glVertex2f(0f, 0f)

        GL11.glColor3f(color2.red / 255f, color2.green / 255f, color2.blue / 255f)
        GL11.glVertex2f(0f, heightF)

        GL11.glColor3f(color3.red / 255f, color3.green / 255f, color3.blue / 255f)
        GL11.glVertex2f(widthF, heightF)

        GL11.glColor3f(color4.red / 255f, color4.green / 255f, color4.blue / 255f)
        GL11.glVertex2f(widthF, 0f)

        GL11.glEnd()
        GL11.glShadeModel(GL11.GL_FLAT)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }

    private fun drawStars() {
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GL11.glPointSize(1.5f)
        GL11.glBegin(GL11.GL_POINTS)

        stars.forEach {
            GL11.glColor4f(1f, 1f, 1f, 0.6f)
            GL11.glVertex2f(it.x, it.y)
            it.y += it.speed
            if (it.y > height) {
                it.y = 0f
                it.x = Random.nextFloat() * width
            }
        }

        GL11.glEnd()
        GlStateManager.enableTexture2D()
    }

    private data class Star(var x: Float, var y: Float, var speed: Float, val z: Float)
}
