package org.sketchfx.canvas

import javafx.beans.InvalidationListener
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.geometry.Bounds
import javafx.scene.transform.Scale
import javafx.scene.transform.Transform
import javafx.scene.transform.Translate
import org.sketchfx.shape.Shape

class CanvasViewModel(private val model: CanvasModel): CanvasContext() {

    fun shapes(): ObservableList<Shape> = model.shapes

    val transformProperty: ObjectProperty<Transform> = SimpleObjectProperty(buildTransform())
    val boundsInParentProperty: ObjectProperty<Bounds> = SimpleObjectProperty()

    private fun buildTransform(): Transform {
        return model.scaleProperty.get().createConcatenation(model.translateProperty.get())
    }

    override var scale: Double
        get() = model.scaleProperty.get().x
        set(newScale) {
            if (newScale >= 0) {
                val bip = this.boundsInParentProperty.get()
                model.scaleProperty.set(Scale(newScale, newScale, bip.getCenterX(), bip.getCenterY()))
            }
        }

    var translate: Pair<Double, Double>
        get() {
            val t = model.translateProperty.get()
            return Pair(t.x, t.y)
        }
        set(newTranslate) {
            model.translateProperty.set(Translate(newTranslate.first, newTranslate.second))
        }

    init {
        val updateTransform = InvalidationListener{
            transformProperty.set(buildTransform())
        }
        model.scaleProperty.addListener(updateTransform)
        model.translateProperty.addListener(updateTransform)
    }
}