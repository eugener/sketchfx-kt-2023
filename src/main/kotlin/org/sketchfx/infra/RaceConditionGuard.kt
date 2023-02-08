package org.sketchfx.infra


open class RaceConditionGuard {

    private var raceStarter: Any? = null

    fun runWith(starter: Any, action: () -> Unit ) {
        if ( raceStarter == null) {
            raceStarter = starter
            try {
                action()
            } finally {
                raceStarter = null
            }
        }
    }

}