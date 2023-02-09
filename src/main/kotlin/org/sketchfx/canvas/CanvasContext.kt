
package org.sketchfx.canvas

import javafx.scene.transform.Transform
import org.sketchfx.cmd.CmdRelocateShapes
import org.sketchfx.event.*
import org.sketchfx.infra.CommandManager
import org.sketchfx.infra.EventBus
import org.sketchfx.infra.SelectionModel
import org.sketchfx.shape.Shape

abstract class CanvasContext {

    val eventBus: EventBus = EventBus()
    val selection: SelectionModel<Shape> = SelectionModel()
    val commandManager: CommandManager = CommandManager()

    init {
        selection.onChange{ fireSelectionChange()}

        eventBus.subscribe(::selectionAddHandler)
        eventBus.subscribe(::shapeRelocatedHandler)
        eventBus.subscribe(::selectionBoundsHandler)

    }

    abstract var scale: Double
    abstract var translate: Pair<Double, Double>
    abstract val transform: Transform

    abstract fun shapes(): Collection<Shape>

    fun fireSelectionChange() {
        eventBus.publish(SelectionChanged(selection.items()))
    }

    fun fireSelectionRelocated() {
        eventBus.publish(SelectionRelocated(selection.items()))
    }

    private fun selectionBoundsHandler(e: SelectionBounds) {
        val selectedShapes = shapes().parallelStream().filter{it.boundsInParent.intersects(e.bounds)}
        selection.set(*selectedShapes.toList().toTypedArray())
    }

    private fun selectionAddHandler(e: SelectionUpdate) {
        if (!e.toggle) {
            if (!selection.contains(e.shape)) {
                selection.set(e.shape)
            }
        } else {
            selection.toggle(e.shape)
        }
    }

    private fun shapeRelocatedHandler(e: ShapeRelocated) {
        val cmd = CmdRelocateShapes(selection.items(), e.dx, e.dy, this)
        if (e.temp) {
            cmd.run()
        } else {
            commandManager.add(cmd)
        }
    }


}