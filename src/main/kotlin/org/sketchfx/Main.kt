package org.sketchfx

import atlantafx.base.theme.PrimerLight
import javafx.application.Application
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import org.sketchfx.canvas.CanvasModel
import org.sketchfx.editor.EditorView
import org.sketchfx.editor.EditorViewModel
import org.sketchfx.fx.action.Action
import org.sketchfx.fx.action.asMenuBar


fun main(args: Array<String>) {
    Application.launch(App::class.java,  *args)
}

class App: Application() {

    private val tabs = TabPane()
    private val currentEditorProperty: ObjectProperty<EditorView?> = SimpleObjectProperty<EditorView?>(null)

    override fun start(primaryStage: Stage?) {

        setUserAgentStylesheet(STYLESHEET_MODENA)

        val undoAction = Action.of("Undo",  accelerator = "meta+Z"){  getCurrentEditor()?.undo() }
        val redoAction = Action.of("Redo",  accelerator = "meta+shift+Z"){  getCurrentEditor()?.redo() }

        val menuActions:List<Action> = listOf(
            Action.group( "Edit",
                undoAction,
                redoAction
            ),
        )



        currentEditorProperty.bind(tabs.selectionModel.selectedItemProperty().map { it?.content as? EditorView? })
        currentEditorProperty.addListener { _, oldEditor, newEditor ->

            oldEditor?.apply {
                undoAction.disabledProperty.unbind()
                redoAction.disabledProperty.unbind()
            }

            newEditor?.apply {
                undoAction.disabledProperty.bind(undoAvailableProperty.not())
                redoAction.disabledProperty.bind(redoAvailableProperty.not())
            }
        }

        tabs.tabs.setAll(
            newTab("Canvas 1", CanvasModel()),
            newTab("Canvas 2", CanvasModel()),
            newTab("Canvas 3", CanvasModel()),
        )
        tabs.selectionModel.select(0)


        val menuBar = menuActions.asMenuBar().apply {
            isUseSystemMenuBar = true
        }
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

}
