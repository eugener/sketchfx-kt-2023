package org.sketchfx.infra

import java.util.concurrent.atomic.AtomicBoolean

open class RaceConditionResolver {

    private val leftLock = AtomicBoolean(false)
    private val rightLock = AtomicBoolean(false)

    private fun guard(lockA: AtomicBoolean, lockB: AtomicBoolean, action: () -> Unit ) {
        if (!lockA.get()) {
            lockB.set(true)
            try {
                action()
            } finally {
                lockB.set(false)
            }
        }
    }

    protected fun guardLeft(action: () -> Unit ): Unit =
        guard(leftLock, rightLock, action)

    protected fun guardRight( action: () -> Unit ) : Unit =
        guard(rightLock, leftLock, action)




}