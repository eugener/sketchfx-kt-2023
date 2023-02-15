package org.sketchfx.event

import org.sketchfx.infra.Event
import javafx.geometry.Bounds
import org.sketchfx.shape.Shape


data class ShapeHover( val base: Shape, val on: Boolean): Event
data class ShapeRelocated(val shape: Shape, val dx: Double, val dy: Double, val temp: Boolean): Event

data class SelectionChanged( val selection: Collection<Shape>): Event
data class SelectionRelocated(val selection: Collection<Shape>): Event
data class SelectionUpdate(val shape: Shape, val toggle: Boolean): Event
data class SelectionBounds(val bounds: Bounds): Event

data class SelectionBand(val bounds: Bounds, val on: Boolean ): Event
data class BasicSelectionShapeAdd(val shape: Shape, val temp: Boolean ): Event