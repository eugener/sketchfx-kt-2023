package org.sketchfx.canvas

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.transform.Scale
import javafx.scene.transform.Translate
import org.sketchfx.shape.Shape

class CanvasModel {

    val shapes: ObservableList<Shape> = FXCollections.observableArrayList()

    val scaleProperty: ObjectProperty<Scale> = SimpleObjectProperty(Scale(1.0,1.0))
    val translateProperty: ObjectProperty<Translate> = SimpleObjectProperty(Translate(0.0, 0.0))

}