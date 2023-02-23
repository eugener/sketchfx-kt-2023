package org.sketchfx.editor

import javafx.geometry.BoundingBox
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SplitPane
import javafx.scene.control.ToolBar
import javafx.scene.layout.BorderPane
import org.sketchfx.canvas.CanvasModel
import org.sketchfx.canvas.CanvasView
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.NodeExt.setupSceneLifecycle
import org.sketchfx.fx.Spacer
import org.sketchfx.fx.action.Action
import org.sketchfx.fx.action.asToolbar
import org.sketchfx.fx.bindingLifecycle
import org.sketchfx.infra.Icons
import org.sketchfx.shape.BasicShapeType
import org.sketchfx.shape.Shape
import kotlin.random.Random

class EditorView( viewModel: EditorViewModel) : BorderPane() {

    private val canvasView = CanvasView(viewModel)
    private val shapeListView = ShapeListView(viewModel)

    private val status = Label("").apply {
        prefWidth = Double.MAX_VALUE
        styleClass.setAll("status-bar")
    }

    private val canvasToolBarLeft = ToolBar().apply {
        items.setAll(
            Spacer.horizontal(),
            Button(null, Icons.APP.graphic()),
        )
    }

    private val toolbarActions: List<Action> = listOf(
        Action.group(
            null, buildGraphic = { Icons.NEW_SHAPE.graphic() },
            Action.of("Rectangle") { canvasView.addBasicShape(BasicShapeType.RECTANGLE) },
            Action.of("Ellipse"  ) { canvasView.addBasicShape(BasicShapeType.OVAL) },
        ),
        Action.SPACER,
        Action.group( "Zoom", buildGraphic = {null},
            Action.of("Zoom In", accelerator = "meta+PLUS") { canvasView.context.scale *= 2 },
            Action.of("Zoom Out", accelerator = "meta+MINUS") { canvasView.context.scale /= 2 },
        ).apply {
            textProperty.bind(canvasView.context.transformProperty.map {  "%.0f%%".format(it.mxx * 100) })
        }
    )

    val undoAvailableProperty = canvasView.context.commandManager.undoAvailableProperty
    val redoAvailableProperty = canvasView.context.commandManager.redoAvailableProperty

    init {
        styleClass.setAll("editor-view")

        center = SplitPane().apply {
            setDividerPositions(.2)
            items.setAll(
                BorderPane(shapeListView, canvasToolBarLeft, null, null, null),
                BorderPane(canvasView, toolbarActions.asToolbar(),null, null, null),
            )
        }
        bottom = status
        setupSceneLifecycle(
            canvasView.context.transformProperty.bindingLifecycle { updateStatus() },
            bindingLifecycle({updateStatus()}, {})
        )

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

    init {
        // TODO for testing only
        model.shapes.setAll(
            rect(100.0, 100.0, this),
            oval(150.0, 200.0, this),
            rect(300.0, 300.0, this),
        )

    }

}

private fun rect(x: Double, y: Double, ctx: CanvasViewModel): Shape =
    Shape.rectangle(BoundingBox(Random.nextDouble(100.0) + x, Random.nextDouble(100.0) + y, 100.0, 100.0), ctx)

private fun oval(x: Double, y: Double, ctx: CanvasViewModel): Shape =
    Shape.oval(BoundingBox(Random.nextDouble(100.0) + x, Random.nextDouble(100.0) + y, 200.0, 100.0), ctx)

