package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object DelayRemover : Module("DelayRemover", Category.COMBAT) {

    private val left by boolean("Left", true)
    private val right by boolean("Right", true)

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        if (left) {
            mc.leftClickCounter = 0
        }

        if (right) {
            mc.rightClickDelayTimer = 0
        }
    }
}
