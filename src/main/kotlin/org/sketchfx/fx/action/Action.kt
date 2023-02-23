package org.sketchfx.fx.action

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCombination
import org.sketchfx.fx.Spacer

interface Action {
    val textProperty: StringProperty
    val graphicProperty: ObjectProperty<Node?>
    val acceleratorProperty: StringProperty
    val disabledProperty: BooleanProperty
    fun execute()

    fun asMenuItem(): MenuItem {
        return MenuItem().apply {
            textProperty().bind(textProperty)
            graphicProperty().bind(graphicProperty)
            acceleratorProperty().bind(acceleratorProperty.map { KeyCombination.keyCombination(it) })
            setOnAction { execute() }
        }
    }

    companion object{

        @JvmStatic
        fun of(text: String?, buildGraphic: GraphicBuilder = { null }, accelerator: String? = null, action: () -> Unit = {}): Action {
            return ActionBase(text, buildGraphic, accelerator, action)
        }

        @JvmStatic
        fun group(text: String?, buildGraphic: GraphicBuilder = { null }, vararg children: Action): Action {
            return ActionGroup(text, buildGraphic, *children)
        }

        @JvmStatic
        fun group(text: String?, vararg children: Action): Action {
            return ActionGroup(text, {null}, *children)
        }

        @JvmStatic val SEPARATOR: Action = ActionBase(null, { null })
        @JvmStatic val SPACER: Action = ActionBase(null, { null })
    }

}

typealias GraphicBuilder = () -> Node?



private open class ActionBase(text: String?, buildGraphic: GraphicBuilder = { null }, accelerator: String? = null, private val action: () -> Unit = {}) :
    Action {
    override val textProperty        = SimpleStringProperty(text)
    override val graphicProperty     = SimpleObjectProperty<Node?>(buildGraphic())
    override val acceleratorProperty = SimpleStringProperty(accelerator)
    override val disabledProperty    = SimpleBooleanProperty(true)
    override fun execute() = action()
}


private class ActionGroup(text: String?, buildGraphic: GraphicBuilder = { null }, vararg val children: Action) :
    ActionBase(text, buildGraphic) {

    fun asMenuButton(): MenuButton {
        return MenuButton().apply {
            this.textProperty().bind(this@ActionGroup.textProperty)
            graphicProperty().bind(this@ActionGroup.graphicProperty)
            children.forEach { a ->
                items.add(a.asMenuItem())
            }
        }
    }

    fun asMenu(): Menu {
        return Menu().apply {
            this.textProperty().bind(this@ActionGroup.textProperty)
//            graphicProperty().bind(this@ActionGroup.graphicProperty)
            children.forEach { a ->
                when (a) {
                    is ActionGroup -> items.add( a.asMenu())
                    is ActionBase  -> items.add(a.asMenuItem())
                    Action.SEPARATOR -> items.add(SeparatorMenuItem())
                    else -> {
                        throw IllegalArgumentException("Unknown action type: ${a.javaClass}")
                    }
                }
            }
        }
    }
}

fun List<Action>.asToolbar(): ToolBar {
    val nodes: List<Node> = this.map {
        when (it) {
            is ActionGroup -> it.asMenuButton()
//            is Action -> it.asMenuItem()
            Action.SEPARATOR -> Separator()
            Action.SPACER -> Spacer.horizontal()
            else -> {
                throw IllegalArgumentException("Unknown action type: ${it.javaClass}")
            }
        }
    }
    return ToolBar().apply {
        items.setAll(nodes)
    }
}

fun List<Action>.asMenuBar(): MenuBar {
    val menus: List<Menu> = this.map {
        when (it) {
            is ActionGroup -> it.asMenu()
//            is ActionBase -> it.asMenuItem()
//            Action.SEPARATOR -> SeparatorMenuItem()
            else -> {
                throw IllegalArgumentException("Unknown action type: ${it.javaClass}")
            }
        }
    }
    return MenuBar().apply {
        getMenus().addAll(menus)
    }
}


