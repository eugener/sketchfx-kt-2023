package org.sketchfx.infra

import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import java.util.*

open class CommandManager<T>( private val context: T ) {

    private val undoStack = Stack<Command<T>>()
    private val redoStack = Stack<Command<T>>()

    private val undoAvailablePropertyWrapper = ReadOnlyBooleanWrapper(undoStack.isNotEmpty())
    val undoAvailableProperty: ReadOnlyBooleanProperty = undoAvailablePropertyWrapper.readOnlyProperty

    private val redoAvailablePropertyWrapper = ReadOnlyBooleanWrapper(redoStack.isNotEmpty())
    val redoAvailableProperty: ReadOnlyBooleanProperty = redoAvailablePropertyWrapper.readOnlyProperty

    private fun updateProps() {
        undoAvailablePropertyWrapper.set(undoStack.isNotEmpty())
        redoAvailablePropertyWrapper.set(redoStack.isNotEmpty())
    }

    fun execute(cmd: Command<T>) {
        cmd.run(context)
        add(cmd)
    }

    fun add(cmd: Command<T>) {
        undoStack.push(cmd)
        redoStack.clear()
        updateProps()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val cmd = undoStack.pop()
            cmd.undo(context)
            redoStack.push(cmd)
            updateProps()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.clear()
            val cmd = redoStack.pop()
            execute(cmd)
            updateProps()
        }
    }

}