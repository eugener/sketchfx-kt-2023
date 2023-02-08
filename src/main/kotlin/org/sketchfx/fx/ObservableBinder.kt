package org.sketchfx.fx

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import org.sketchfx.infra.RaceConditionGuard

/**
 * Simplifies bidirectional binding for 2 entities
 * They don't have to implement Observable interface, instead that conversion should be provided using asObservable method
 * Update method should implement actual update action for the provided source entity
 */
abstract class ObservableBinder(private val left: Any, private val right: Any) {

    private val guard = RaceConditionGuard()

    protected abstract fun asObservable(source: Any): Observable
    protected abstract fun update(source: Any)

    private val leftListener = InvalidationListener { _ ->
        guard.runWith(right) {
           update(left)
        }
    }

    private val rightListener = InvalidationListener { _ ->
        guard.runWith(left) {
            update(right)
        }
    }

    fun bind() {
        asObservable(left).addListener(leftListener)
        asObservable(right).addListener(rightListener)
    }

    fun unbind() {
        asObservable(left).removeListener(leftListener)
        asObservable(right).removeListener(rightListener)
    }

}