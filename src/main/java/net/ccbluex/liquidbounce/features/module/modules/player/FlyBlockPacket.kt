package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.BlinkUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.*
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color

object FlyBlockPacket : Module("FlyBlockPacket", Category.PLAYER) {

    private val visibleLimit by int("VisibleBlocks", 4, 0..10)

    private var placedCount = 0
    private var limitBlock = true

    init {
        EventManager.registerEventHook(PacketEvent::class.java, EventHook(this, false, 0) { event ->
            val packet = event.packet

            if (mc.thePlayer == null || mc.thePlayer!!.isDead) return@EventHook

            if (event.eventType == EventState.SEND) {
                when (packet) {
                    is C03PacketPlayer -> {
                        BlinkUtils.blink(packet, event, true, false)
                    }
                    is C08PacketPlayerBlockPlacement -> {
                        if (limitBlock) {
                            if (placedCount < visibleLimit) {
                                placedCount++
                            } else {
                                BlinkUtils.blink(packet, event, true, false)
                            }
                        }
                    }
                }
            }

            if (event.eventType == EventState.RECEIVE) {
                if (isServerPacket(packet) && !isEntityMovementPacket(packet)) {
                    BlinkUtils.blink(packet, event, false, true)
                }
            }
        })

        EventManager.registerEventHook(MotionEvent::class.java, EventHook(this, false, 0) { event ->
            if (event.eventState == EventState.POST) {
                val thePlayer = mc.thePlayer ?: return@EventHook

                if (thePlayer.isDead || thePlayer.ticksExisted <= 10) {
                    BlinkUtils.unblink()
                } else {
                    BlinkUtils.syncReceived()                                      // credit : DeletedUser , BeoPhiMan
                }
            }
        })

        EventManager.registerEventHook(Render3DEvent::class.java, EventHook(this, false, 0) {
            val positions = BlinkUtils.positions
            val color = Color(150, 200, 255, 180)

            synchronized(positions) {
                GL11.glPushMatrix()
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                mc.entityRenderer.disableLightmap()
                GL11.glBegin(GL11.GL_LINE_STRIP)
                RenderUtils.glColor(color)

                val renderPosX = mc.renderManager.renderPosX
                val renderPosY = mc.renderManager.renderPosY
                val renderPosZ = mc.renderManager.renderPosZ

                for (vec in positions) {
                    GL11.glVertex3d(
                        vec.xCoord - renderPosX,
                        vec.yCoord - renderPosY,
                        vec.zCoord - renderPosZ
                    )
                }

                GL11.glEnd()
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDisable(GL11.GL_LINE_SMOOTH)
                GL11.glDisable(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_TEXTURE_2D)
                GL11.glPopMatrix()
            }
        })
    }

    override fun onEnable() {
        placedCount = 0
    }

    override fun onDisable() {
        BlinkUtils.unblink()
    }

    override val tag: String
        get() = BlinkUtils.packetsReceived.size.toString()

    fun blinkingSend(): Boolean = false
    fun blinkingReceive(): Boolean = handleEvents()

    private fun isServerPacket(packet: Any): Boolean {
        return packet.javaClass.simpleName.startsWith("S")
    }

    private fun isEntityMovementPacket(packet: Any): Boolean {
        return when (packet) {
            is S14PacketEntity,
            is S18PacketEntityTeleport,
            is S19PacketEntityHeadLook,
            is S0BPacketAnimation,
            is S0CPacketSpawnPlayer,
            is S1CPacketEntityMetadata -> true
            else -> {
                val name = packet.javaClass.simpleName
                name == "S15PacketEntityRelMove" ||
                        name == "S17PacketEntityLookMove" ||
                        name == "S16PacketEntityLook"
            }
        }
    }
}