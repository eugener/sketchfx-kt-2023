package org.sketchfx.canvas

import org.sketchfx.event.SelectionChanged
import org.sketchfx.infra.CommandManager
import org.sketchfx.infra.EventBus
import org.sketchfx.infra.SelectionModel
import org.sketchfx.shape.Shape

abstract class CanvasContext() {

    val eventBus: EventBus = EventBus()
    val selection: SelectionModel<Shape> = SelectionModel()
    val commandManager: CommandManager = CommandManager()

    init {
        selection.onChange{ fireSelectionChange()}
    }

    abstract var scale: Double

    fun fireSelectionChange() {
        eventBus.publish(SelectionChanged(selection.items()))
    }

}