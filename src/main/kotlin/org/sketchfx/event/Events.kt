package org.sketchfx.event

import org.sketchfx.infra.Event
import javafx.geometry.Bounds
import javafx.geometry.Point2D
import org.sketchfx.shape.Shape

data class SelectionBand(val bounds: Bounds, val on: Boolean ): Event
data class BasicSelectionShapeAdd(val shape: Shape, val mousePosition: Point2D, val temp: Boolean): Event