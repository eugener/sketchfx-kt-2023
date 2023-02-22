package org.sketchfx

import atlantafx.base.theme.PrimerLight
import javafx.application.Application
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import org.sketchfx.canvas.CanvasModel
import org.sketchfx.editor.EditorView
import org.sketchfx.editor.EditorViewModel
import org.sketchfx.infra.Icons


fun main(args: Array<String>) {
    Application.launch(App::class.java,  *args)
}

class App: Application() {

    private val tabs = TabPane()
    private val currentEditorProperty: ObjectProperty<EditorView?> = SimpleObjectProperty<EditorView?>(null)

    override fun start(primaryStage: Stage?) {

        setUserAgentStylesheet(STYLESHEET_MODENA)

        val undoMenu = newMenuItem("Undo", "meta+Z", Icons.UNDO.graphic()) {
            getCurrentEditor()?.undo()
        }
        val redoMenu = newMenuItem("Redo", "meta+shift+Z", Icons.REDO.graphic()) {
            getCurrentEditor()?.redo()
        }

        val editMenu = Menu("Edit", null,
            undoMenu,
            redoMenu
        )

        val menuBar = MenuBar(editMenu).apply {
            isUseSystemMenuBar = true
        }

        currentEditorProperty.bind(tabs.selectionModel.selectedItemProperty().map { it?.content as? EditorView? })
        currentEditorProperty.addListener { _, oldEditor, newEditor ->

            oldEditor?.apply {
                undoMenu.disableProperty().unbind()
                redoMenu.disableProperty().unbind()
            }

            newEditor?.apply {
                undoMenu.disableProperty().bind(undoAvailableProperty.not())
                redoMenu.disableProperty().bind(redoAvailableProperty.not())
            }
        }

        tabs.tabs.setAll(
            newTab("Canvas 1", CanvasModel()),
            newTab("Canvas 2", CanvasModel()),
            newTab("Canvas 3", CanvasModel()),
        )
        tabs.selectionModel.select(0)


        val browser = BorderPane(tabs, menuBar, null, null, null)

        val scene = javafx.scene.Scene(browser, 1000.0, 600.0)
        setUserAgentStylesheet(PrimerLight().userAgentStylesheet)
//        setUserAgentStylesheet(PrimerDark().userAgentStylesheet)
        scene.stylesheets.add(App::class.java.getResource("styles.css")?.toExternalForm())

        primaryStage?.scene = scene
        primaryStage?.title = "SketchFX"
        primaryStage?.show()

    }

    private fun getCurrentEditor(): EditorView? {
        return currentEditorProperty.get()
    }

    private fun newTab(title: String, model: CanvasModel): Tab {
        return Tab(title, EditorView(EditorViewModel(model))).apply {
            userData = model
        }
    }

    private fun newMenuItem(menuTitle: String, menuAccelerator: String, graphic: Node, action: () -> Unit): MenuItem {
        return MenuItem(menuTitle).apply {
            this.graphic = graphic
            onAction = EventHandler { action() }
            accelerator = KeyCombination.keyCombination(menuAccelerator)
        }
    }
}
