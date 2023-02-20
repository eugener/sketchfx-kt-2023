package org.sketchfx.canvas.layer

import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.scene.Group
import javafx.scene.control.Label
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.canvas.NewShapeAvatar
import org.sketchfx.fx.NodeExt.setupSceneLifecycle
import org.sketchfx.fx.SceneLifecycle
import org.sketchfx.shape.SelectionBox
import org.sketchfx.shape.Shape

class OverlayCanvasLayer(private val context: CanvasViewModel): CanvasLayer(), SceneLifecycle {

    private val hoverGroup = Group()
    private val selectionGroup = Group()
    private val bandGroup = Group()

    private val sizeLabel = Label().apply {
        styleClass.add("shape-size-label")
        isMouseTransparent = true
    }

    init {
        this.group.children.addAll(hoverGroup, selectionGroup, bandGroup)
        setupSceneLifecycle()
    }

    override fun onSceneSet() {
        context.hoveredShapeProperty.addListener(shapeHoverHandler)
        context.selection.items().addListener(selectionChangeHandler)
        context.selectionBandProperty.addListener(selectionBandHandler)
        context.newShapeAvatarProperty.addListener(newShapeAvatarHandler)
    }

    override fun onSceneUnset() {
        context.hoveredShapeProperty.removeListener(shapeHoverHandler)
        context.selection.items().removeListener(selectionChangeHandler)
        context.selectionBandProperty.removeListener(selectionBandHandler)
        context.newShapeAvatarProperty.removeListener(newShapeAvatarHandler)
    }

    private val selectionBandHandler = ChangeListener{_, _, bounds ->
        if (bounds != null) {
            bandGroup.children.setAll(Shape.selectionBand(bounds, context))
            context.updateSelection(bounds)
        } else {
            bandGroup.children.clear()
        }
    }

    private val shapeHoverHandler: ChangeListener<Shape?> = ChangeListener { _, _, shape ->
        if (shape != null) {
            if (!context.selection.contains(shape)) {
                showHover(shape)
            }
        } else {
            hideHover()
        }
    }
    private val selectionChangeHandler = InvalidationListener {
        showSelection(context.selection.items())
    }

    private val newShapeAvatarHandler = ChangeListener<NewShapeAvatar?> { _, _, shapeAvatar ->
        if (shapeAvatar != null) {
            with(sizeLabel) {
                val shapeBounds = shapeAvatar.shape.boundsInParent
                text = "%.0f x %.0f".format(shapeBounds.width, shapeBounds.height)
                layoutX = shapeAvatar.mousePosition.x + 5
                layoutY = shapeAvatar.mousePosition.y + 5
            }
            bandGroup.children.setAll(shapeAvatar.shape, sizeLabel)
        } else {
            bandGroup.children.clear()
        }
    }

    private fun hideHover() {
        hoverGroup.children.clear()
    }

    private fun showHover( shape: Shape) {
        hoverGroup.children.setAll(Shape.hover(shape))
    }

    private fun showSelection(selection: Collection<Shape>) {
        if (selection.isEmpty()) {
            selectionGroup.children.clear()
        } else {
            hideHover()
            selectionGroup.children.setAll(SelectionBox(selection, context))
        }
    }


}