package org.sketchfx.fx

import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import kotlin.reflect.KProperty


class ObjectPropertyDelegate<T>(private val fxprop: ObjectProperty<T>) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return fxprop.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        fxprop.set(value)
    }
}

fun <T> ObjectProperty<T>.delegate(): ObjectPropertyDelegate<T> {
    return ObjectPropertyDelegate(this)
}

class DoublePropertyDelegate(private val fxprop: DoubleProperty) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        return fxprop.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
        fxprop.set(value)
    }
}

fun DoubleProperty.delegate(): DoublePropertyDelegate {
    return DoublePropertyDelegate(this)
}