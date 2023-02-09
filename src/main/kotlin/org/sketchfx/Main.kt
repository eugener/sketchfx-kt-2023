package org.sketchfx

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import org.sketchfx.canvas.CanvasModel
import org.sketchfx.editor.EditorView
import org.sketchfx.editor.EditorViewModel

fun main(args: Array<String>) {
    Application.launch(App::class.java,  *args)
}

class App: Application() {

    private val tabs = TabPane()


    override fun start(primaryStage: Stage?) {

        setUserAgentStylesheet(STYLESHEET_MODENA)


        val undoMenu = buildMenu("Undo", "meta+Z") {
            getCurrentEditor()?.undo()
        }
        val redoMenu = buildMenu("Redo", "meta+shift+Z") {
            getCurrentEditor()?.redo()
        }


        val editMenu = Menu("Edit", null,
            undoMenu,
            redoMenu
        )

        val menuBar = MenuBar(editMenu)
        menuBar.isUseSystemMenuBar = true

        tabs.selectionModel.selectedItemProperty().addListener { _, oldTab, newTab ->

            getEditor(oldTab)?.apply {
                undoMenu.disableProperty().unbind()
                redoMenu.disableProperty().unbind()
            }

            getEditor(newTab)?.apply {
                undoMenu.disableProperty().bind(undoAvailableProperty.not())
                redoMenu.disableProperty().bind(redoAvailableProperty.not())
            }
        }

        tabs.tabs.setAll(
            buildTab("Canvas 1", CanvasModel()),
            buildTab("Canvas 2", CanvasModel()),
            buildTab("Canvas 3", CanvasModel()),
        )
        tabs.selectionModel.select(0)


        val browser = BorderPane(tabs, menuBar, null, null, null)

        val scene = javafx.scene.Scene(browser, 1000.0, 600.0)
        scene.stylesheets.add(App::class.java.getResource("styles.css")?.toExternalForm())

        primaryStage?.scene = scene
        primaryStage?.title = "SketchFX"
        primaryStage?.show()

    }

    private fun getEditor( tab: Tab?): EditorView? {
        return tab?.content as EditorView?
    }

    private fun getCurrentEditor(): EditorView? {
        return getEditor(tabs.selectionModel.selectedItem)
    }

    private fun buildTab(title: String, model: CanvasModel): Tab {
        return Tab(title, EditorView(EditorViewModel(model))).apply {
            userData = model
            isClosable = false
        }
    }

    private fun buildMenu(title: String, accelerator: String, action: () -> Unit): MenuItem {
        val item = MenuItem(title)
        item.onAction = EventHandler{ action() }
        item.accelerator = KeyCombination.keyCombination(accelerator)
        return item
    }
}
