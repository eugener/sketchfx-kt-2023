
package org.sketchfx.canvas

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.transform.Transform
import org.sketchfx.cmd.CmdRelocateShapes
import org.sketchfx.event.SelectionBounds
import org.sketchfx.event.SelectionChanged
import org.sketchfx.event.SelectionRelocated
import org.sketchfx.event.SelectionUpdate
import org.sketchfx.fx.delegate
import org.sketchfx.infra.CommandManager
import org.sketchfx.infra.EventBus
import org.sketchfx.infra.SelectionModel
import org.sketchfx.shape.BasicShapeType
import org.sketchfx.shape.Shape

abstract class CanvasContext {

    val eventBus: EventBus = EventBus().apply {
        subscribe(::selectionAddHandler)
        subscribe(::selectionBoundsHandler)
    }
    val selection: SelectionModel<Shape> = SelectionModel<Shape>().apply {
        onChange{ fireSelectionChange()}
    }
    val commandManager: CommandManager<CanvasContext> = CommandManager(this)

    val mouseDragModeProperty: ObjectProperty<MouseDragMode> = SimpleObjectProperty(MouseDragMode.SELECTION)
    var mouseDragMode by mouseDragModeProperty.delegate()

    var basicShape = BasicShapeType.RECTANGLE

    abstract var scale: Double
    abstract var translate: Pair<Double, Double>
    abstract val transform: Transform

    abstract fun shapes(): MutableList<Shape>

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

    fun shapeRelocated( shape: Shape, dx: Double, dy: Double, temp: Boolean) {
        val cmd = CmdRelocateShapes(selection.items(), dx, dy)
        if (temp) {
            cmd.run(this)
        } else {
            commandManager.add(cmd)
        }
    }



}