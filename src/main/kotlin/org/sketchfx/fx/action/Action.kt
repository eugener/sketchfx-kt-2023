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
    var graphic: Node?
        get() = graphicProperty.get()
        set(value) = graphicProperty.set(value)

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
            disableProperty().bind(disabledProperty)
            setOnAction { action?.invoke()}
        }
    }

    fun asButton(): Button {
        return Button().apply {
            textProperty().bind(textProperty)
            graphicProperty().bind(graphicProperty)
            disableProperty().bind(disabledProperty)
            setOnAction { action?.invoke()}
        }
    }

    companion object{

        @JvmStatic
        fun of( text: String? = null, buildGraphic: GraphicBuilder = { null }): Action = ActionImpl(text, buildGraphic)

        @JvmStatic
        fun group(buildGraphic: GraphicBuilder): ActionGroup = ActionGroupImpl(null, buildGraphic)

        @JvmStatic
        fun group(text: String?): ActionGroup = ActionGroupImpl(text) { null }

        @JvmStatic val SEPARATOR: Action = ActionImpl(null) { null }

        @JvmStatic val SPACER: Action = ActionImpl(null) { null }
    }

}

interface ActionGroup: Action {
    var actions: List<Action>
}

private typealias GraphicBuilder = () -> Node?


private open class ActionImpl(text: String?, buildGraphic: GraphicBuilder = { null }) : Action {
    override val textProperty        = SimpleStringProperty(text)
    override val graphicProperty     = SimpleObjectProperty<Node?>(buildGraphic())
    override val acceleratorProperty = SimpleStringProperty()
    override val disabledProperty    = SimpleBooleanProperty(false)
    override var action: (() -> Unit)? = null
}


private class ActionGroupImpl(text: String?, buildGraphic: GraphicBuilder = { null }) :
    ActionImpl(text, buildGraphic), ActionGroup {

    override var actions: List<Action> = mutableListOf()

    fun asMenuButton(): MenuButton {
        return MenuButton().apply {
            this.textProperty().bind(this@ActionGroupImpl.textProperty)
            graphicProperty().bind(this@ActionGroupImpl.graphicProperty)
            disableProperty().bind(disabledProperty)
            items.setAll( actions.map{a-> a.asMenuItem()})
        }
    }

    fun asMenu(): Menu {
        return Menu().apply {
            this.textProperty().bind(this@ActionGroupImpl.textProperty)
//            graphicProperty().bind(this@ActionGroup.graphicProperty)
            disableProperty().bind(disabledProperty)
            actions.forEach { a ->
                when (a) {
                    Action.SEPARATOR   -> items.add(SeparatorMenuItem())
                    is ActionGroupImpl -> items.add( a.asMenu())
                    else -> {
                        items.add(a.asMenuItem())
                    }
                }
            }
        }
    }
}

fun List<Action>.asToolbar(): ToolBar {
    return ToolBar().apply {
        val nodes: List<Node> = this@asToolbar.map {
            when (it) {
                Action.SEPARATOR -> Separator()
                Action.SPACER -> Spacer.horizontal()
                is ActionGroupImpl -> it.asMenuButton()
                else -> {
                    it.asButton()
                }
            }
        }
        items.setAll(nodes)
    }
}

fun List<Action>.asMenuBar(): MenuBar {
    return MenuBar().apply {
        val menus: List<Menu> = this@asMenuBar.map { a ->
            when (a) {
                is ActionGroupImpl -> a.asMenu()
//            is ActionBase -> it.asMenuItem()
//            Action.SEPARATOR -> SeparatorMenuItem()
                else -> {
                    throw IllegalArgumentException("Unknown action type: ${a.javaClass}")
                }
            }
        }
        getMenus().addAll(menus)
    }
}


