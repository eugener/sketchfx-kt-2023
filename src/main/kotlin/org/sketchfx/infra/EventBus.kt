package org.sketchfx.infra

interface Event

class EventBus {

    val allSubscribers = mutableMapOf<Class<out Event>, MutableSet<(Event) -> Unit>>()

    inline fun <reified T: Event> subscribers(): MutableSet<(T) -> Unit> {
        @Suppress("UNCHECKED_CAST")
        return allSubscribers.getOrPut( T::class.java) { mutableSetOf() } as MutableSet<(T) -> Unit>
    }

    inline fun <reified T:  Event> subscribe(noinline subscriber: (T) -> Unit) {
        subscribers<T>().add(subscriber)
    }

    inline fun <reified T: Event> unsubscribe(noinline subscriber: (T) -> Unit) {
        subscribers<T>().remove(subscriber)
    }

    inline fun <reified T: Event> publish(event: T) {
        val subscribersForEvent = allSubscribers[T::class.java] ?: emptySet()
        subscribersForEvent.forEach { it(event) }
    }
}

