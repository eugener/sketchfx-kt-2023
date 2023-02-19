
package org.sketchfx.canvas

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.transform.Transform
import org.sketchfx.cmd.CmdRelocateShapes
import org.sketchfx.event.SelectionBounds
import org.sketchfx.event.SelectionRelocated
import org.sketchfx.fx.delegate
import org.sketchfx.infra.CommandManager
import org.sketchfx.infra.EventBus
import org.sketchfx.infra.SelectionModel
import org.sketchfx.shape.BasicShapeType
import org.sketchfx.shape.Shape

abstract class CanvasContext {

    val eventBus: EventBus = EventBus().apply {
        subscribe(::selectionBoundsHandler)
    }
    val selection = SelectionModel<Shape>()
    val commandManager: CommandManager<CanvasContext> = CommandManager(this)

    val mouseDragModeProperty: ObjectProperty<MouseDragMode> = SimpleObjectProperty(MouseDragMode.SELECTION)
    var mouseDragMode by mouseDragModeProperty.delegate()

    var basicShape = BasicShapeType.RECTANGLE

    abstract var scale: Double
    abstract var translate: Pair<Double, Double>
    abstract val transform: Transform

    abstract fun shapes(): MutableList<Shape>


    fun fireSelectionRelocated() {
        eventBus.publish(SelectionRelocated(selection.items()))
    }

    private fun selectionBoundsHandler(e: SelectionBounds) {
        val selectedShapes = shapes().parallelStream().filter{it.boundsInParent.intersects(e.bounds)}
        selection.set(*selectedShapes.toList().toTypedArray())
    }

    fun selectionUpdate(shape: Shape, toggle: Boolean) {
        if (!toggle) {
            if (!selection.contains(shape)) {
                selection.set(shape)
            }
        } else {
            selection.toggle(shape)
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