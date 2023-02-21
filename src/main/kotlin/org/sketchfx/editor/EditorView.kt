package org.sketchfx.editor

import atlantafx.base.theme.Styles
import atlantafx.base.theme.Tweaks
import javafx.beans.InvalidationListener
import javafx.geometry.BoundingBox
import javafx.scene.control.*
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.util.Callback
import org.sketchfx.canvas.CanvasModel
import org.sketchfx.canvas.CanvasView
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.*
import org.sketchfx.fx.MultipleSelectionModelExt.bidirectionalBindingLifecycle
import org.sketchfx.fx.NodeExt.setupSceneLifecycle
import org.sketchfx.shape.BasicShapeType
import org.sketchfx.shape.Shape
import kotlin.random.Random

class EditorView( private val viewModel: EditorViewModel) : BorderPane() {

    private val status = Label("").apply {
        prefWidth = Double.MAX_VALUE
        styleClass.setAll("status-bar")
    }

    private val zoomMenu = MenuButton("Zoom").apply {
        items.setAll(
            MenuItem("Zoom In").apply {
                setOnAction { canvasView.context.scale *= 2 }
                accelerator = KeyCombination.keyCombination("meta+PLUS")
          },
            MenuItem("Zoom Out").apply {
                setOnAction { canvasView.context.scale /= 2 }
                accelerator = KeyCombination.keyCombination("meta+MINUS")
            },
        )
    }

    private val canvasToolBarLeft = ToolBar().apply {
        items.setAll(
            Spacer.horizontal(),
            MenuButton("Shape").apply {
                BasicShapeType.values().forEach { shape ->
                    items.add(MenuItem(shape.title()).apply {
                        setOnAction { canvasView.addBasicShape(shape) }
                    })
                }
            }
        )
    }

    private val canvasToolBarRight = ToolBar().apply {
        items.setAll(
            Spacer.horizontal(),
            zoomMenu
        )
    }
    private val canvasView = CanvasView(viewModel)

    private val shapeListView = ListView<Shape>().apply {
        styleClass.addAll(Tweaks.EDGE_TO_EDGE, Styles.DENSE, "shape-list-view")
        cellFactory = Callback{ShapeListCell()}
        selectionModel.selectionMode = SelectionMode.MULTIPLE
    }


    val undoAvailableProperty = canvasView.context.commandManager.undoAvailableProperty
    val redoAvailableProperty = canvasView.context.commandManager.redoAvailableProperty

    private val statusListener = InvalidationListener{updateStatus()}
    private val lifecycleBindings = listOf(
        shapeListView.items.contentBindingLifecycle(viewModel.shapes()),
        shapeListView.selectionModel.bidirectionalBindingLifecycle(viewModel.selection.items()),
        canvasView.context.transformProperty.bindingLifecycle(statusListener),
        bindingLifecycle({updateStatus()}, {})
    )

    init {
        styleClass.setAll("editor-view")

        center = SplitPane().apply {
            setDividerPositions(.2)
            items.setAll(
                BorderPane(shapeListView, canvasToolBarLeft, null, null, null),
                BorderPane(canvasView, canvasToolBarRight,null, null, null),
            )
        }
        bottom = status
        setupSceneLifecycle(lifecycleBindings)

    }

    private fun updateStatus() {
        canvasView.context.transformProperty.get()?.run {
            status.text = "scale: %.2f; translate: (%.2f : %.2f)".format(mxx, tx, ty)
            zoomMenu.text= "%.0f%%".format(mxx * 100)

        }

    }

    fun undo() {
        canvasView.context.commandManager.undo()
    }

    fun redo() {
        canvasView.context.commandManager.redo()
    }

    private inner class ShapeListCell: StringListCell<Shape>("shape-list-cell") {
        init{
            // hovering over the list sell highlights the shape on the canvas
            val context = this@EditorView.viewModel
            val cell = this@ShapeListCell
            this.hoverProperty().addListener{ _, _: Boolean?, isNowHovered: Boolean ->
                if (!cell.isEmpty) {
                    context.hoveredShape = if (isNowHovered) cell.item else null
                }
            }
        }
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

private fun rect(x: Double, y: Double, ctx: CanvasViewModel): Shape =
    Shape.rectangle(BoundingBox(Random.nextDouble(100.0) + x, Random.nextDouble(100.0) + y, 100.0, 100.0), ctx)

private fun oval(x: Double, y: Double, ctx: CanvasViewModel): Shape =
    Shape.oval(BoundingBox(Random.nextDouble(100.0) + x, Random.nextDouble(100.0) + y, 200.0, 100.0), ctx)

