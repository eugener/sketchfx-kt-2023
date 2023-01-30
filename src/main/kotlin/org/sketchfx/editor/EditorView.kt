package org.sketchfx.editor

import javafx.geometry.BoundingBox
import javafx.scene.control.ListView
import javafx.scene.control.SplitPane
import org.sketchfx.canvas.CanvasContext
import org.sketchfx.canvas.CanvasModel
import org.sketchfx.canvas.CanvasView
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.shape.Shape
import kotlin.random.Random

class EditorView(viewModel: EditorViewModel) : SplitPane() {

    private val canvasView = CanvasView(viewModel.canvasViewModel)
    private val shapeListView = ListView<Shape>()

    val undoAvailableProperty   = canvasView.context.commandManager.undoAvailableProperty
    val redoAvailableProperty   = canvasView.context.commandManager.redoAvailableProperty
    val canvasTransformProperty = canvasView.context.transformProperty

    init {
        styleClass.setAll("editor-view")
        items.setAll(shapeListView, canvasView)
        setDividerPositions(.2)
    }

    fun undo() {
        canvasView.context.commandManager.undo()
    }

    fun redo() {
        canvasView.context.commandManager.redo()
    }
}

class EditorViewModel(model: CanvasModel) {

    val canvasViewModel = CanvasViewModel(model)

    init {
        // TODO for testing only
        model.shapes.setAll(
            rect(100.0, 100.0, canvasViewModel),
            oval(150.0, 200.0, canvasViewModel),
            rect(300.0, 300.0, canvasViewModel),
        )

    }

}

private fun rect(x: Double, y: Double, ctx: CanvasContext): Shape =
    Shape.rectangle(BoundingBox(Random.nextDouble(100.0) + x, Random.nextDouble(100.0) + y, 100.0, 100.0), ctx)

private fun oval(x: Double, y: Double, ctx: CanvasContext): Shape =
    Shape.oval(BoundingBox(Random.nextDouble(100.0) + x, Random.nextDouble(100.0) + y, 200.0, 100.0), ctx)