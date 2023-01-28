package org.sketchfx.event

import org.sketchfx.infra.Event
import javafx.geometry.Bounds
import org.sketchfx.shape.Shape

data class ShapeHover(val base: Shape, val on: Boolean): Event
data class ShapeRelocated(val shape: Shape, val dx: Double, val dy: Double, val temp: Boolean): Event

data class SelectionChanged(val selection: Set<Shape>): Event
data class SelectionAdd(val shape: Shape, val toggle: Boolean): Event

data class SelectionBand(val bounds: Bounds, val on: Boolean ): Event