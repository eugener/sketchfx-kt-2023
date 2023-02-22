package org.sketchfx.infra



import javafx.scene.Node
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon

// https://kordamp.org/ikonli/cheat-sheet-bootstrapicons.html
enum class Icons(private val ikon: Ikon) {

    NEW_SHAPE(BootstrapIcons.PLUS_CIRCLE),
    APP(BootstrapIcons.BOX),
    UNDO(BootstrapIcons.ARROW_COUNTERCLOCKWISE),
    REDO(BootstrapIcons.ARROW_CLOCKWISE),
    ;

    fun graphic(): Node = FontIcon.of(ikon)

}