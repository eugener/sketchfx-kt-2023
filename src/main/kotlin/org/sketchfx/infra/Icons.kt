package org.sketchfx.infra



import javafx.scene.Node
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon

// https://kordamp.org/ikonli/cheat-sheet-bootstrapicons.html
enum class Icons(private val ikon: Ikon) {

    APP(BootstrapIcons.BOX),

    // shape creation
    NEW_SHAPE(BootstrapIcons.PLUS_CIRCLE),

    // alignment
    ALIGN_LEFT(BootstrapIcons.ALIGN_START),
    ALIGN_CENTER(BootstrapIcons.ALIGN_CENTER),
    ALIGN_RIGHT(BootstrapIcons.ALIGN_END),
    ALIGN_TOP(BootstrapIcons.ALIGN_TOP),
    ALIGN_MIDDLE(BootstrapIcons.ALIGN_MIDDLE),
    ALIGN_BOTTOM(BootstrapIcons.ALIGN_BOTTOM),

    // command management
    UNDO(BootstrapIcons.ARROW_COUNTERCLOCKWISE),
    REDO(BootstrapIcons.ARROW_CLOCKWISE),
    ;

    fun graphic(): Node = FontIcon.of(ikon)

}