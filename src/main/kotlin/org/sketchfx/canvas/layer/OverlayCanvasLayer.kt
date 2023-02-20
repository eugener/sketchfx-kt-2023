package org.sketchfx.canvas.layer

import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.scene.Group
import javafx.scene.control.Label
import org.sketchfx.canvas.CanvasViewModel
import org.sketchfx.canvas.NewShapeAvatar
import org.sketchfx.fx.NodeExt.setupSceneLifecycle
import org.sketchfx.fx.bindingLifecycle
import org.sketchfx.shape.SelectionBox
import org.sketchfx.shape.Shape

class OverlayCanvasLayer(private val context: CanvasViewModel): CanvasLayer() {

    private val hoverGroup = Group()
    private val selectionGroup = Group()
    private val bandGroup = Group()

    private val sizeLabel = Label().apply {
        styleClass.add("shape-size-label")
        isMouseTransparent = true
    }

//    override fun onSceneSet() {
//        bindings.forEach(Binder::bind)
//    }
//
//    override fun onSceneUnset() {
//        bindings.forEach(Binder::unbind)
//    }

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

    private val lifecycleBindings = listOf(
        context.hoveredShapeProperty.bindingLifecycle(shapeHoverHandler),
        context.selection.items().bindingLifecycle(selectionChangeHandler),
        context.selectionBandProperty.bindingLifecycle(selectionBandHandler),
        context.newShapeAvatarProperty.bindingLifecycle(newShapeAvatarHandler)
    )

    init {
        this.group.children.addAll(hoverGroup, selectionGroup, bandGroup)
        setupSceneLifecycle(lifecycleBindings)
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