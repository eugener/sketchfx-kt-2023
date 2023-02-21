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

private class BindingLifecycleImpl(val bindAction: () -> Unit, val unbindAction: () -> Unit) : BindingLifecycle {
    override fun bind() { bindAction() }
    override fun unbind() { unbindAction() }
}

fun bindingLifecycle(bind: () -> Unit, unbind: () -> Unit): BindingLifecycle = BindingLifecycleImpl(bind, unbind)

fun <T> ObservableValue<T>.bindingLifecycle(listener: ChangeListener<T>): BindingLifecycle {
    return bindingLifecycle( bind = { addListener(listener) }, unbind = { removeListener(listener) })
}

fun <T> Property<T>.bindingLifecycle(prop: ObservableValue<T>): BindingLifecycle {
    return bindingLifecycle( bind = { bind(prop) }, unbind = { unbind() })
}

fun <T> List<T>.contentBindingLifecycle(list: ObservableList<T>): BindingLifecycle {
    return bindingLifecycle(
        bind = { Bindings.bindContent(this, list) },
        unbind = { Bindings.unbindContent(this, list) }
    )
}


fun Observable.bindingLifecycle(listener: InvalidationListener): BindingLifecycle {
    return bindingLifecycle( bind = { addListener(listener) }, unbind = { removeListener(listener) })
}

fun <T: Event> Node.eventHandlerBindingLifecycle(event: EventType<T>, handler: EventHandler<T>): BindingLifecycle {
    return bindingLifecycle( bind = { addEventHandler(event, handler) }, unbind = { removeEventHandler(event, handler) })
}

fun <T: Event> Node.eventFilterBindingLifecycle(event: EventType<T>, handler: EventHandler<T>): BindingLifecycle {
    return bindingLifecycle( bind = { addEventFilter(event, handler) }, unbind = { removeEventFilter(event, handler) })
}
