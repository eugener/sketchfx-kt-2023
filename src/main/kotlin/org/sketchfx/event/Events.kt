package org.sketchfx.event

import javafx.geometry.Point2D
import org.sketchfx.infra.Event
import org.sketchfx.shape.Shape

data class BasicSelectionShapeAdd(val shape: Shape, val mousePosition: Point2D, val temp: Boolean): Event