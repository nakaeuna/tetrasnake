package taniere.tetrasnake

import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

data class FacteursVitesse(
    var multiplicateurSerpent: Double,
    var multiplicateurBlocs: Double,
)

class ControleurTetrasnake(
    private val jeuSerpent: JeuSerpent,
    private val jeuBlocs: JeuBlocs,
    private val canvasSerpent: HTMLCanvasElement,
    private val canvasBlocs: HTMLCanvasElement,
    private val contexteSerpent: CanvasRenderingContext2D,
    private val contexteBlocs: CanvasRenderingContext2D,
    private val elementScoreSerpent: HTMLSpanElement,
    private val elementScoreBlocs: HTMLSpanElement,
    private val elementScoreTotal: HTMLSpanElement,
    private val elementOverlay: HTMLDivElement,
    private val elementTexteGameOver: HTMLDivElement,
    private val boutonRelancer: HTMLButtonElement,
    private val vitesse: FacteursVitesse,
) {
    private var partieTermineeGlobale: Boolean = false
    private var dernierTimestampMs: Double = 0.0
    private val dernierDeclenchementParActionMs = mutableMapOf<String, Double>()

    fun demarrer() {
        boutonRelancer.addEventListener("click", { _: Event ->
            relancerToutesLesParties()
        })

        window.addEventListener("keydown", { e ->
            gererTouchePressee(e as KeyboardEvent)
        })
        window.addEventListener("keyup", { e ->
            gererToucheRelachee(e as KeyboardEvent)
        })

        window.requestAnimationFrame(::boucleDeJeu)
    }

    private fun relancerToutesLesParties() {
        jeuSerpent.reinitialiser()
        jeuBlocs.reinitialiser()
        jeuBlocs.setDescenteAccelereeMaintenue(false)
        partieTermineeGlobale = false
        elementOverlay.classList.add("hidden")
    }

    private fun peutDeclencherAction(action: String, minGapMs: Double): Boolean {
        val maintenant = window.performance.now()
        val dernier = dernierDeclenchementParActionMs[action] ?: Double.NEGATIVE_INFINITY
        if (maintenant - dernier < minGapMs) return false
        dernierDeclenchementParActionMs[action] = maintenant
        return true
    }

    private fun gererTouchesSerpent(touchePressee : String) {
        when (touchePressee) {
            "z", "Z" -> {
                if (peutDeclencherAction("snake-haut", 40.0)) {
                    jeuSerpent.gererDeplacementHautSerpent()
                }
            }
            "q", "Q" -> {
                if (peutDeclencherAction("snake-gauche", 40.0)) {
                    jeuSerpent.gererDeplacementGaucheSerpent()
                }
            }
            "s", "S" -> {
                if (peutDeclencherAction("snake-bas", 40.0)) {
                    jeuSerpent.gererDeplacementBasSerpent()
                }
            }
            "d", "D" -> {
                if (peutDeclencherAction("snake-droite", 40.0)) {
                    jeuSerpent.gererDeplacementDroiteSerpent()
                }
            }
        }
    }

    private fun gererTouchesBlocs(touchePressee : String, event : KeyboardEvent) {
        when (touchePressee) {
            "ArrowLeft" -> {
                event.preventDefault()
                if (peutDeclencherAction("bloc-gauche", 40.0)) {
                    jeuBlocs.gererDeplacementGaucheBlocs()
                }
            }
            "ArrowRight" -> {
                event.preventDefault()
                if (peutDeclencherAction("bloc-droite", 40.0)) {
                    jeuBlocs.gererDeplacementDroiteBlocs()
                }
            }
            "ArrowUp" -> {
                event.preventDefault()
                if (peutDeclencherAction("bloc-rotation", 80.0)) {
                    jeuBlocs.gererRotationHoraireBlocs()
                }
            }
            "ArrowDown" -> {
                event.preventDefault()
                jeuBlocs.setDescenteAccelereeMaintenue(true)
            }
        }
    }

    private fun gererTouchePressee(event: KeyboardEvent) {
        val touchePressee = event.key

        if (touchePressee.equals("r", ignoreCase = true)) {
            relancerToutesLesParties()
            return
        }
        if (partieTermineeGlobale) return

        gererTouchesSerpent(touchePressee)
        gererTouchesBlocs(touchePressee, event)
    }

    private fun gererToucheRelachee(event: KeyboardEvent) {
        if (event.key != "ArrowDown") return
        event.preventDefault()
        jeuBlocs.setDescenteAccelereeMaintenue(false)
    }

    private fun calculerDeltaSeconde(timestampMs: Double): Double {
        val deltaSeconde = if (dernierTimestampMs == 0.0) 0.0 else (timestampMs - dernierTimestampMs) / 1000.0
        dernierTimestampMs = timestampMs
        return deltaSeconde
    }

    private fun mettreAJourLogiqueJeux(deltaSeconde: Double) {
        if (partieTermineeGlobale) {
            // On force l'arrêt des descentes accélérées.
            jeuBlocs.setDescenteAccelereeMaintenue(false)
            return
        }

        jeuSerpent.mettreAJour(deltaSeconde * vitesse.multiplicateurSerpent)
        jeuBlocs.mettreAJour(deltaSeconde * vitesse.multiplicateurBlocs)
    }

    private fun verifierFinDePartieEtMettreAJourHUD() {
        if (partieTermineeGlobale) return
        if (!(jeuSerpent.perdu || jeuBlocs.perdu)) return

        partieTermineeGlobale = true
        jeuSerpent.forcerFinDePartie()
        jeuBlocs.forcerFinDePartie()
        elementTexteGameOver.textContent = "GAME OVER"
        elementOverlay.classList.remove("hidden")
    }

    private fun mettreAJourScoresEtRendu() {
        val scoreSerpent = jeuSerpent.score
        val scoreBlocs = jeuBlocs.score
        val scoreTotal = scoreSerpent + scoreBlocs

        elementScoreSerpent.textContent = scoreSerpent.toString()
        elementScoreBlocs.textContent = scoreBlocs.toString()
        elementScoreTotal.textContent = scoreTotal.toString()

        contexteSerpent.clearRect(
            0.0,
            0.0,
            canvasSerpent.width.toDouble(),
            canvasSerpent.height.toDouble(),
        )
        contexteBlocs.clearRect(
            0.0,
            0.0,
            canvasBlocs.width.toDouble(),
            canvasBlocs.height.toDouble(),
        )

        jeuSerpent.dessiner(contexteSerpent)
        jeuBlocs.dessiner(contexteBlocs)
    }

    private fun boucleDeJeu(empreinteDeTempsMilliseconde: Double) {
        val deltaSeconde = calculerDeltaSeconde(empreinteDeTempsMilliseconde)
        mettreAJourLogiqueJeux(deltaSeconde)
        verifierFinDePartieEtMettreAJourHUD()
        mettreAJourScoresEtRendu()
        window.requestAnimationFrame(::boucleDeJeu)
    }
}

