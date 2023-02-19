package org.sketchfx.event

import org.sketchfx.infra.Event
import javafx.geometry.Bounds
import javafx.geometry.Point2D
import org.sketchfx.shape.Shape

data class SelectionRelocated(val selection: Collection<Shape>): Event
data class SelectionUpdate(val shape: Shape, val toggle: Boolean): Event
data class SelectionBounds(val bounds: Bounds): Event

data class SelectionBand(val bounds: Bounds, val on: Boolean ): Event
data class BasicSelectionShapeAdd(val shape: Shape, val mousePosition: Point2D, val temp: Boolean): Event