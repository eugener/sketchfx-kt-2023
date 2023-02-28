package org.sketchfx.canvas

import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.geometry.*
import javafx.scene.transform.Scale
import javafx.scene.transform.Transform
import javafx.scene.transform.Translate
import org.sketchfx.cmd.CmdAlignShapes
import org.sketchfx.cmd.CmdRelocateShapes
import org.sketchfx.fx.delegate
import org.sketchfx.infra.CommandManager
import org.sketchfx.infra.SelectionModel
import org.sketchfx.shape.BasicShapeType
import org.sketchfx.shape.Shape

open class CanvasViewModel(private val model: CanvasModel) {

//    val eventBus: EventBus = EventBus()
    val selection = SelectionModel<Shape> { s ->
        arrayOf(
            s.boundsInParentProperty(), // update selection band on selected shape bounds change
            transformProperty           // allows to adjust selection band elements by scale
        )
    }

    val commandManager: CommandManager<CanvasViewModel> = CommandManager(this)

    val mouseDragModeProperty: ObjectProperty<MouseDragMode> = SimpleObjectProperty(MouseDragMode.SELECTION)
    var mouseDragMode by mouseDragModeProperty.delegate()

    var basicShapeToAdd = BasicShapeType.RECTANGLE


    // the list of shapes on the canvas
    fun shapes(): ObservableList<Shape> = model.shapes

    // represents current transform of the canvas
    val transformProperty: ObjectProperty<Transform> = SimpleObjectProperty(buildTransform()).apply {
        val transformBinding = Bindings.createObjectBinding( ::buildTransform, model.scaleProperty, model.translateProperty)
        bind(transformBinding)
        //TODO unbind
    }

    // represents the bounds of the canvas in parent coordinates
    val boundsInParentProperty: ObjectProperty<Bounds> = SimpleObjectProperty()

    // builds the transform from the scale and translate
    private fun buildTransform(): Transform {
        return model.scaleProperty.get().createConcatenation(model.translateProperty.get())
    }

    val transform: Transform
        get() = transformProperty.get()

    // the scale exposed as a double
    var scale: Double
        get() = model.scaleProperty.get().x
        set(newScale) {
            if (newScale >= 0) {
                val bip = this.boundsInParentProperty.get()
                model.scaleProperty.set(Scale(newScale, newScale, bip.centerX, bip.centerY))
            }
        }

    // translate exposed as a pair of doubles
    var translate: Pair<Double, Double>
        get() {
            val t = model.translateProperty.get()
            return Pair(t.x, t.y)
        }
        set(newTranslate) {
            model.translateProperty.set(Translate(newTranslate.first, newTranslate.second))
        }

    // the shape that is currently being hovered over
    val hoveredShapeProperty: ObjectProperty<Shape?> = SimpleObjectProperty()
    var hoveredShape by hoveredShapeProperty.delegate()

    val selectionBandProperty: ObjectProperty<Bounds?> = SimpleObjectProperty()
    var selectionBand by selectionBandProperty.delegate()

    val newShapeAvatarProperty: ObjectProperty<NewShapeAvatar?> = SimpleObjectProperty()
    var newShapeAvatar by newShapeAvatarProperty.delegate()

    // update selection based on the bounds
    fun updateSelection(bounds: Bounds) {
        val selectedShapes = shapes().parallelStream().filter{it.boundsInParent.intersects(bounds)}
        selection.set(*selectedShapes.toList().toTypedArray())
    }

    // update selection based on the shape adding or toggling
    fun updateSelection(shape: Shape, toggle: Boolean) {
        if (!toggle) {
            if (!selection.contains(shape)) {
                selection.set(shape)
            }
        } else {
            selection.toggle(shape)
        }
    }

    // relocate selection by the given delta
    fun relocateSelection(source: Shape, dx: Double, dy: Double, temp: Boolean) {
        val cmd = CmdRelocateShapes(selection.items(), dx, dy)
        if (temp) {
            cmd.run(this)
        } else {
            commandManager.add(cmd)
        }
    }

    fun alignSelection(alignment: Alignment ) {
        commandManager.execute( CmdAlignShapes(selection.items(), alignment))
    }


}

data class NewShapeAvatar(val shape: Shape, val mousePosition: Point2D)