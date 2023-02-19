package org.sketchfx.canvas

import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.geometry.Bounds
import javafx.scene.transform.Scale
import javafx.scene.transform.Transform
import javafx.scene.transform.Translate
import org.sketchfx.fx.delegate
import org.sketchfx.shape.Shape

open class CanvasViewModel(private val model: CanvasModel): CanvasContext() {

    // the list of shapes on the canvas
    override fun shapes(): ObservableList<Shape> = model.shapes

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

    override val transform: Transform
        get() = transformProperty.get()

    // the scale exposed as a double
    override var scale: Double
        get() = model.scaleProperty.get().x
        set(newScale) {
            if (newScale >= 0) {
                val bip = this.boundsInParentProperty.get()
                model.scaleProperty.set(Scale(newScale, newScale, bip.centerX, bip.centerY))
            }
        }

    // translate exposed as a pair of doubles
    override var translate: Pair<Double, Double>
        get() {
            val t = model.translateProperty.get()
            return Pair(t.x, t.y)
        }
        set(newTranslate) {
            model.translateProperty.set(Translate(newTranslate.first, newTranslate.second))
        }


    // the shape that is currently being hovered over
    val shapeHoverProperty: ObjectProperty<Shape?> = SimpleObjectProperty()
    var shapeHover by shapeHoverProperty.delegate()

}