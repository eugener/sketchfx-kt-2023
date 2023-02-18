package org.sketchfx.infra

interface Command<T> {
    fun run(context: T)
    fun undo(context: T)
}