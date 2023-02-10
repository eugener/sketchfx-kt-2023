package org.sketchfx.editor

import javafx.beans.InvalidationListener
import javafx.beans.binding.Bindings
import javafx.geometry.BoundingBox
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import javafx.util.Callback
import org.sketchfx.canvas.CanvasContext
import org.sketchfx.canvas.CanvasModel
import org.sketchfx.canvas.CanvasView
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.MultipleSelectionModelExt.bindBidirectional
import org.sketchfx.fx.MultipleSelectionModelExt.unbindBidirectional
import org.sketchfx.fx.StringListCell
import org.sketchfx.shape.Shape
import kotlin.random.Random

class EditorView(viewModel: EditorViewModel) : BorderPane() {

    private val status = Label("").apply {
        prefWidth = Double.MAX_VALUE
        styleClass.setAll("status-bar")
    }

    private val canvasView = CanvasView(viewModel)

    private val shapeListView = ListView<Shape>().apply {
        cellFactory = Callback{ StringListCell<Shape>("shape-list-cell") }
        selectionModel.selectionMode = javafx.scene.control.SelectionMode.MULTIPLE
    }

    val undoAvailableProperty   = canvasView.context.commandManager.undoAvailableProperty
    val redoAvailableProperty   = canvasView.context.commandManager.redoAvailableProperty

    init {
        styleClass.setAll("editor-view")

        val splitPane = SplitPane().apply {
            items.setAll(shapeListView, canvasView)
            setDividerPositions(.2)
        }

        center = splitPane
        bottom = status

        val statusListener = InvalidationListener{updateStatus()}

        val canvasTransformProperty = canvasView.context.transformProperty

        sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) {
                Bindings.bindContent(shapeListView.items, viewModel.shapes())
                shapeListView.selectionModel.bindBidirectional(viewModel.selection.items())
                canvasTransformProperty.addListener(statusListener)
                updateStatus()
            } else {
                Bindings.unbindContent(shapeListView.items, viewModel.shapes())
                shapeListView.selectionModel.unbindBidirectional(viewModel.selection.items())
                canvasTransformProperty.removeListener(statusListener)
            }
        }
    }

    private fun updateStatus() {
        canvasView.context.transformProperty.get()?.run {
            status.text = "scale: %.2f; translate: (%.2f : %.2f)".format(mxx, tx, ty)
        }

    }

    fun undo() {
        canvasView.context.commandManager.undo()
    }

    fun redo() {
        canvasView.context.commandManager.redo()
    }
}

class EditorViewModel(model: CanvasModel): CanvasViewModel(model) {

//    val canvasViewModel = CanvasViewModel(model)

    init {
        // TODO for testing only
        model.shapes.setAll(
            rect(100.0, 100.0, this),
            oval(150.0, 200.0, this),
            rect(300.0, 300.0, this),
        )

    }

}

private fun rect(x: Double, y: Double, ctx: CanvasContext): Shape =
    Shape.rectangle(BoundingBox(Random.nextDouble(100.0) + x, Random.nextDouble(100.0) + y, 100.0, 100.0), ctx)

private fun oval(x: Double, y: Double, ctx: CanvasContext): Shape =
    Shape.oval(BoundingBox(Random.nextDouble(100.0) + x, Random.nextDouble(100.0) + y, 200.0, 100.0), ctx)
