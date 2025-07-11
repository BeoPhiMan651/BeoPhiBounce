package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe
import net.minecraft.network.play.client.*
import org.lwjgl.input.Keyboard

object heromc : SpeedMode("heromc") {

    private const val BOOST_CONSTANT = 0.00125
    private var ticks = 0
    private var validTicks = 0
    private var startDelayTicks = 0

    override fun onEnable() {
        ticks = 0
        validTicks = 0
        startDelayTicks = 3
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        forceStopSprinting()
    }

    override fun onStrafe() {
        val player = mc.thePlayer ?: return
        if (player.onGround && player.isMoving) {
            player.tryJump()
        }
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        val input = player.movementInput

        val forward = input.moveForward
        val isW = Keyboard.isKeyDown(Keyboard.KEY_W)
        val isS = Keyboard.isKeyDown(Keyboard.KEY_S)

        val isLegitMove = isW && !isS && forward > 0f

        if (startDelayTicks > 0) {
            startDelayTicks--
            mc.timer.timerSpeed = 1.0f
            return
        }

        if (!isLegitMove || !player.isMoving || player.isInLiquid || player.isInWeb || player.isOnLadder) {
            validTicks = 0
            mc.timer.timerSpeed = 1.0f
            player.isSprinting = forward > 0.8f
            sendStopSprinting()
            return
        }

        validTicks++

        if (!player.isSprinting && forward > 0.8f) {
            player.isSprinting = true
            sendStartSprinting()
        }

        if (validTicks >= 1) {
            if (player.onGround) {
                player.motionY = 0.42 - if (Speed.intaveLowHop) 1.7E-14 else 0.0
                strafe(strength = Speed.strafeStrength.toDouble())
                mc.timer.timerSpeed = Speed.groundTimer * 0.985f
            } else {
                mc.timer.timerSpeed = Speed.airTimer * 0.985f
            }

            if (Speed.boost && player.motionY > 0.003 && player.isSprinting && isLegitMove) {
                player.motionX *= 1.0 + (BOOST_CONSTANT * Speed.initialBoostMultiplier)
                player.motionZ *= 1.0 + (BOOST_CONSTANT * Speed.initialBoostMultiplier)
            }

            if (ticks % 2 == 0 && player.onGround) {
                spoofPackets()
            }
        } else {
            mc.timer.timerSpeed = 1.0f
        }

        ticks++
    }

    private fun spoofPackets() {
        val player = mc.thePlayer ?: return
        mc.netHandler.addToSendQueue(C03PacketPlayer(true))
        mc.netHandler.addToSendQueue(C00PacketKeepAlive((System.nanoTime() and 0x7FFFFFFF).toInt()))
        mc.netHandler.addToSendQueue(C0FPacketConfirmTransaction(0, Short.MIN_VALUE, false))
    }

    private fun sendStartSprinting() {
        val player = mc.thePlayer ?: return
        mc.netHandler.addToSendQueue(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.START_SPRINTING))
    }

    private fun sendStopSprinting() {
        val player = mc.thePlayer ?: return
        mc.netHandler.addToSendQueue(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.STOP_SPRINTING))
    }

    private fun forceStopSprinting() {
        val player = mc.thePlayer ?: return
        player.isSprinting = false
        sendStopSprinting()
    }
}
