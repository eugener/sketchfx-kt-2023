package org.sketchfx.editor

import atlantafx.base.theme.Styles
import atlantafx.base.theme.Tweaks
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.util.Callback
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.fx.MultipleSelectionModelExt.bidirectionalBindingLifecycle
import org.sketchfx.fx.NodeExt.setupSceneLifecycle
import org.sketchfx.fx.StringListCell
import org.sketchfx.fx.contentBindingLifecycle
import org.sketchfx.shape.Shape

class ShapeListView( private val viewModel: CanvasViewModel ): ListView<Shape>() {

    private val lifecycleBindings = listOf(
        items.contentBindingLifecycle(viewModel.shapes()),
        selectionModel.bidirectionalBindingLifecycle(viewModel.selection.items()),
    )


    init {
        styleClass.addAll("shape-list-view", Tweaks.EDGE_TO_EDGE, Styles.DENSE )
        cellFactory = Callback{ShapeListCell()}
        selectionModel.selectionMode = SelectionMode.MULTIPLE
        setupSceneLifecycle(lifecycleBindings)
    }


    private inner class ShapeListCell: StringListCell<Shape>("shape-list-cell") {
        init{
            // hovering over the list sell highlights the shape on the canvas
            val context = this@ShapeListView.viewModel
            val cell = this@ShapeListCell
            this.hoverProperty().addListener{ _, _: Boolean?, isNowHovered: Boolean ->
                if (!cell.isEmpty) {
                    context.hoveredShape = if (isNowHovered) cell.item else null
                }
            }
        }
    }

}