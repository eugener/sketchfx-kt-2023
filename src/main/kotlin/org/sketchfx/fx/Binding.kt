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

interface Binding {
    fun bind()
    fun unbind()
}

fun simpleBinding(bind: () -> Unit, unbind: () -> Unit): Binding {
    return object : Binding {
        override fun bind() {
            bind()
        }

        override fun unbind() {
            unbind()
        }
    }
}

fun <T> ObservableValue<T>.binding(listener: ChangeListener<T>): Binding {
    return object: Binding {
        override fun bind() {
            addListener(listener)
        }

        override fun unbind() {
            removeListener(listener)
        }
    }
}

fun <T> Property<T>.binding(prop: ObservableValue<T>): Binding {
    return object: Binding {
        override fun bind() {
            bind(prop)
        }

        override fun unbind() {
            unbind()
        }
    }
}

fun <T> List<T>.contentBinding(list: ObservableList<T>): Binding {
    return object : Binding {
        override fun bind() {
            Bindings.bindContent(this@contentBinding, list)
        }

        override fun unbind() {
            Bindings.unbindContent(this@contentBinding, list)
        }
    }
}


fun Observable.binding(listener: InvalidationListener): Binding {
    return object: Binding {
        override fun bind() {
            addListener(listener)
        }

        override fun unbind() {
            removeListener(listener)
        }
    }
}

fun <T: Event> Node.eventHandlerBinding(event: EventType<T>, handler: EventHandler<T>): Binding {
    return object: Binding {
        override fun bind() {
            addEventHandler(event, handler)
        }

        override fun unbind() {
            removeEventHandler(event, handler)
        }
    }
}

fun <T: Event> Node.eventFilterBinding(event: EventType<T>, handler: EventHandler<T>): Binding {
    return object: Binding {
        override fun bind() {
            addEventFilter(event, handler)
        }

        override fun unbind() {
            removeEventFilter(event, handler)
        }
    }
}
