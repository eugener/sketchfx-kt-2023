package org.sketchfx.fx

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Node

// Represents a binding lifecycle between two objects
// Usually relevant within Scene lifecycle where the binding should be established when the scene is set
// and removed when the scene is unset
interface BindingLifecycle {
    fun bind()
    fun unbind()
}

fun simpleBindingLifecycle(bind: () -> Unit, unbind: () -> Unit): BindingLifecycle {
    return object : BindingLifecycle {
        override fun bind() { bind() }
        override fun unbind() { unbind() }
    }
}

fun <T> ObservableValue<T>.bindingLifecycle(listener: ChangeListener<T>): BindingLifecycle {
    return simpleBindingLifecycle( bind = { addListener(listener) }, unbind = { removeListener(listener) })
}

fun <T> Property<T>.bindingLifecycle(prop: ObservableValue<T>): BindingLifecycle {
    return simpleBindingLifecycle( bind = { bind(prop) }, unbind = { unbind() })
}

fun <T> List<T>.contentBindingLifecycle(list: ObservableList<T>): BindingLifecycle {
    return simpleBindingLifecycle(
        bind = { Bindings.bindContent(this@contentBindingLifecycle, list) },
        unbind = { Bindings.unbindContent(this@contentBindingLifecycle, list) }
    )
}


fun Observable.bindingLifecycle(listener: InvalidationListener): BindingLifecycle {
    return simpleBindingLifecycle( bind = { addListener(listener) }, unbind = { removeListener(listener) })
}

fun <T: Event> Node.eventHandlerBindingLifecycle(event: EventType<T>, handler: EventHandler<T>): BindingLifecycle {
    return simpleBindingLifecycle( bind = { addEventHandler(event, handler) }, unbind = { removeEventHandler(event, handler) })
}

fun <T: Event> Node.eventFilterBindingLifecycle(event: EventType<T>, handler: EventHandler<T>): BindingLifecycle {
    return simpleBindingLifecycle( bind = { addEventFilter(event, handler) }, unbind = { removeEventFilter(event, handler) })
}
