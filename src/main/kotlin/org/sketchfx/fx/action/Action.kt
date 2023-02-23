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
    var text: String?
        get() = textProperty.get()
        set(value) = textProperty.set(value)

    val graphicProperty: ObjectProperty<Node?>

    val acceleratorProperty: StringProperty
    var accelerator: String?
        get() = acceleratorProperty.get()
        set(value) = acceleratorProperty.set(value)

    val disabledProperty: BooleanProperty
    var disabled: Boolean
        get() = disabledProperty.get()
        set(value) = disabledProperty.set(value)

    var action: (() -> Unit)?

    fun asMenuItem(): MenuItem {
        return MenuItem().apply {
            textProperty().bind(textProperty)
            graphicProperty().bind(graphicProperty)
            acceleratorProperty().bind(acceleratorProperty.map { KeyCombination.keyCombination(it) })
            setOnAction { action?.invoke()}
        }
    }

    companion object{

        @JvmStatic
        fun of( text: String?, buildGraphic: GraphicBuilder = { null }): Action {
            return ActionBase(text, buildGraphic)
        }

        @JvmStatic
        fun group(buildGraphic: GraphicBuilder = { null }): ActionGroup {
            return ActionGroupBase(null, buildGraphic)
        }

        @JvmStatic
        fun group(text: String?): ActionGroup {
            return ActionGroupBase(text, {null})
        }

        @JvmStatic val SEPARATOR: Action = ActionBase(null, { null })
        @JvmStatic val SPACER: Action = ActionBase(null, { null })
    }

}

interface ActionGroup: Action {
    var actions: List<Action>
}

typealias GraphicBuilder = () -> Node?



private open class ActionBase(text: String?, buildGraphic: GraphicBuilder = { null }) : Action {
    override val textProperty        = SimpleStringProperty(text)
    override val graphicProperty     = SimpleObjectProperty<Node?>(buildGraphic())
    override val acceleratorProperty = SimpleStringProperty()
    override val disabledProperty    = SimpleBooleanProperty(true)
    override var action: (() -> Unit)? = null
}


private class ActionGroupBase(text: String?, buildGraphic: GraphicBuilder = { null }) :
    ActionBase(text, buildGraphic), ActionGroup {

    override var actions: List<Action> = mutableListOf()

    fun asMenuButton(): MenuButton {
        return MenuButton().apply {
            this.textProperty().bind(this@ActionGroupBase.textProperty)
            graphicProperty().bind(this@ActionGroupBase.graphicProperty)
            actions.forEach { a ->
                items.add(a.asMenuItem())
            }
        }
    }

    fun asMenu(): Menu {
        return Menu().apply {
            this.textProperty().bind(this@ActionGroupBase.textProperty)
//            graphicProperty().bind(this@ActionGroup.graphicProperty)
            actions.forEach { a ->
                when (a) {
                    is ActionGroupBase -> items.add( a.asMenu())
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
            is ActionGroupBase -> it.asMenuButton()
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
            is ActionGroupBase -> it.asMenu()
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


