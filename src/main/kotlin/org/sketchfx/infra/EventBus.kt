package org.sketchfx.infra

interface Event
private typealias Subscriber = (Event) -> Unit
private typealias SubscriberSet = Set<Subscriber>
private typealias EventClass = Class<out Event>

class EventBus {



    val allSubscribers = mutableMapOf<EventClass, SubscriberSet>()

    inline fun <reified T: Event> subscribersFor(): SubscriberSet {
        return allSubscribers[T::class.java] ?: emptySet()
    }

    inline fun <reified T: Event> subscribe(noinline subscriber: Subscriber) {
        allSubscribers[T::class.java] = subscribersFor<T>() + subscriber
    }

    inline fun <reified T: Event> unsubscribe(noinline subscriber: Subscriber) {
        allSubscribers[T::class.java] = subscribersFor<T>() - subscriber
    }

    inline fun <reified T: Event> publish(event: T) {
        subscribersFor<T>().forEach { it(event) }
    }
}

