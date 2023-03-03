package org.sketchfx.editor.props

import javafx.beans.InvalidationListener
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Bounds
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import org.sketchfx.fx.IntTextField
import org.sketchfx.fx.delegate
import org.sketchfx.shape.Shape

class ShapeBoundsPane: GridPane() {

    private val boundShapeProperty: ObjectProperty<Shape?> = SimpleObjectProperty()
    var boundShape by boundShapeProperty.delegate()

    private val shapeBoundsProperty: ObjectProperty<Bounds?> = SimpleObjectProperty()

    private val xPosTextField = intField("X")
    private val yPosTextField = intField("Y")
    private val wPosTextField = intField("W")
    private val hPosTextField = intField("H")


    init {

        styleClass.addAll("shape-bounds-pane", "sub-pane")

        // layout fields
        add(xPosTextField, 0, 0)
        add(yPosTextField, 1, 0)
        add(wPosTextField, 0, 1)
        add(hPosTextField, 1, 1)

        // show shape bounds when shape is selected
        boundShapeProperty.addListener { _, oldShape, newShape ->
            oldShape?.let {
                shapeBoundsProperty.unbind()
            }
            newShape?.let {
                shapeBoundsProperty.bind(newShape.boundsInParentProperty())
            } ?: run {
                xPosTextField.text = ""
                yPosTextField.text = ""
                wPosTextField.text = ""
                hPosTextField.text = ""
            }
        }

        // update shape bounds when field values change
        shapeBoundsProperty.addListener { _, _, newBounds ->
            newBounds?.let {
                xPosTextField.value = it.minX.toInt()
                yPosTextField.value = it.minY.toInt()
                wPosTextField.value = it.width.toInt()
                hPosTextField.value = it.height.toInt()
            }
        }

        // update field values when shape bounds change
        val updateShapeBounds = InvalidationListener{ _ ->
            //TODO convert to a command and add to undo stack
            boundShape?.resizeRelocate(
                xPosTextField.value.toDouble(),
                yPosTextField.value.toDouble(),
                wPosTextField.value.toDouble(),
                hPosTextField.value.toDouble()
            )
        }
        xPosTextField.valueProperty.addListener(updateShapeBounds)
        yPosTextField.valueProperty.addListener(updateShapeBounds)
        wPosTextField.valueProperty.addListener(updateShapeBounds)
        hPosTextField.valueProperty.addListener(updateShapeBounds)

    }

    private fun intField(label: String): IntTextField {
        return IntTextField().apply {
            styleClass.add("prop-field")
            disableProperty().bind(boundShapeProperty.isNull)
            right = Label(label).apply { isDisable = true }
        }
    }

}