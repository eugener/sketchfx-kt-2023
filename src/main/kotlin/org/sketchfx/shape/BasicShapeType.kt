package org.sketchfx.shape

import java.util.*

enum class BasicShapeType() {
    RECTANGLE,
    OVAL;

    fun title(): String {
        return this.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }


}