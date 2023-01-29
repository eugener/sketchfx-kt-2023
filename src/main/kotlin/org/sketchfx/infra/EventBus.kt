package org.sketchfx.infra

interface Event

class EventBus {

    val allSubscribers = mutableMapOf<Class<out Event>, Set<(Event) -> Unit>>()

    inline fun <reified T: Event> subscribersFor(): Set<(T) -> Unit> {
        return allSubscribers[T::class.java] ?: emptySet()
    }

    inline fun <reified T:  Event> subscribe(noinline subscriber: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        allSubscribers[T::class.java] = (subscribersFor<T>() + subscriber) as Set<(Event) -> Unit>
    }

    inline fun <reified T: Event> unsubscribe(noinline subscriber: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        allSubscribers[T::class.java] = (subscribersFor<T>() + subscriber) as Set<(Event) -> Unit>
    }

    inline fun <reified T: Event> publish(event: T) {
        subscribersFor<T>().forEach { it(event) }
    }
}

