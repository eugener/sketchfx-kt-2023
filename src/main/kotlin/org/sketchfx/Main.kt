package org.sketchfx

import org.sketchfx.canvas.CanvasContext
import org.sketchfx.canvas.CanvasModel
import org.sketchfx.canvas.CanvasView
import org.sketchfx.canvas.CanvasViewModel
import javafx.application.Application
import javafx.beans.InvalidationListener
import javafx.event.EventHandler
import javafx.geometry.BoundingBox
import javafx.scene.control.*
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import org.sketchfx.shape.Shape
import kotlin.random.Random

fun main(args: Array<String>) {
    Application.launch(App::class.java,  *args)
}

class App: Application() {

    private val tabs = TabPane()
    private val status = Label("")

    override fun start(primaryStage: Stage?) {

        setUserAgentStylesheet(STYLESHEET_MODENA)

        status.prefWidth = Double.MAX_VALUE
        status.styleClass.setAll("status-bar")
        val statusListener = InvalidationListener{updateStatus()}

        val undoMenu = buildMenu("Undo", "meta+Z") {
            getCurrentCanvas()?.context?.commandManager?.undo()
        }
        val redoMenu = buildMenu("Redo", "meta+shift+Z") {
            getCurrentCanvas()?.context?.commandManager?.redo()
        }


        val editMenu = Menu("Edit", null,
            undoMenu,
            redoMenu
        )

        val menuBar = MenuBar(editMenu)
        menuBar.isUseSystemMenuBar = true

        tabs.getSelectionModel().selectedItemProperty().addListener { _, oldTab, newTab ->

            oldTab?.let {
                when(val canvas = it.content) {
                    is CanvasView -> {
                        undoMenu.disableProperty().unbind()
                        redoMenu.disableProperty().unbind()
                        canvas.canvasTransformProperty.removeListener(statusListener)
                    }
                }
            }

            newTab?.let {
                when(val canvas = it.content) {
                    is CanvasView -> {
                        undoMenu.disableProperty().bind(canvas.context.commandManager.undoAvailableProperty.not())
                        redoMenu.disableProperty().bind(canvas.context.commandManager.redoAvailableProperty.not())
                        canvas.canvasTransformProperty.addListener(statusListener)
                        statusListener.invalidated(null)
                    }
                }
            }
        }
        tabs.tabs.setAll(
            buildTab("Canvas 1", CanvasModel()),
            buildTab("Canvas 2", CanvasModel()),
            buildTab("Canvas 3", CanvasModel()),
        )
        val browser = BorderPane(tabs, menuBar, null, status, null)
        val scene = javafx.scene.Scene(browser, 1000.0, 600.0)
        scene.stylesheets.add(App::class.java.getResource("styles.css")?.toExternalForm())

        primaryStage?.scene = scene
        primaryStage?.title = "SketchFX"
        primaryStage?.show()


    }

    private fun getCurrentCanvas(): CanvasView? {
        return when(val c = tabs.selectionModel.selectedItem.content) {
            is CanvasView -> c
            else -> null
        }
    }

    private fun updateStatus() {
        val transform = getCurrentCanvas()?.canvasTransformProperty?.get()
        transform?.let {
            status.text = "scale: %.2f; translate: (%.2f : %.2f)".format(transform.mxx, transform.tx, transform.ty)
        }

    }

    private fun buildTab(title: String, model: CanvasModel): Tab {

        val viewModel = CanvasViewModel(model)
        val canvas = CanvasView(viewModel)

        fun random() = Random.nextDouble(100.0)

        // TODO for testing only
        model.shapes.setAll(
            rect(random() + 100, random() + 100, viewModel),
            oval(random() + 150, random() + 200, viewModel),
            rect(random() + 300, random() + 300, viewModel),
        )

        val tab = Tab(title, canvas)
        tab.userData = model
        tab.isClosable = false
        return tab
    }

    private fun rect(x: Double, y: Double, ctx: CanvasContext): Shape = Shape.rectangle(BoundingBox(x, y, 100.0, 100.0), ctx)
    private fun oval(x: Double, y: Double, ctx: CanvasContext): Shape = Shape.oval(BoundingBox(x, y, 200.0, 100.0), ctx)


    private fun buildMenu(title: String, accelerator: String, action: () -> Unit): MenuItem {
        val item = MenuItem(title)
        item.onAction = EventHandler{ action() }
        item.accelerator = KeyCombination.keyCombination(accelerator)
        return item
    }


}