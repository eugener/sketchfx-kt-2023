package org.sketchfx.infra

interface Command {
    fun run()
    fun undo()
}