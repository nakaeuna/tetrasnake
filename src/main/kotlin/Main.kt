package taniere.tetrasnake

import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLCanvasElement

fun main() {
    val canvasSerpent = ajoutCanvas("canvas-snake")
    val canvasBlocs = ajoutCanvas("canvas-blocks")

    val contexteSerpent = canvasSerpent.getContext("2d") as CanvasRenderingContext2D
    val contexteBlocs = canvasBlocs.getContext("2d") as CanvasRenderingContext2D

    val elementScoreSerpent = ajoutSpan("score-snake")
    val elementScoreBlocs = ajoutSpan("score-tetris")
    val elementScoreTotal = ajoutSpan("score-total")
    val elementOverlay = ajoutDiv("game-overlay")
    val elementTexteGameOver = ajoutDiv("game-over-text")
    val boutonRelancer = ajoutButton("restart-btn")

    val vitesse = FacteursVitesse(multiplicateurSerpent = 1.0, multiplicateurBlocs = 1.0)

    val tailleCelluleSerpentPx = 20.0
    val colonnesSerpent = (canvasSerpent.width / tailleCelluleSerpentPx).toInt()
    val lignesSerpent = (canvasSerpent.height / tailleCelluleSerpentPx).toInt()

    val colonnesBlocs = 10
    val tailleCelluleBlocsPx = canvasBlocs.width.toDouble() / colonnesBlocs.toDouble()
    val hauteurHudBlocsPx = 80.0
    val lignesBlocs = ((canvasBlocs.height.toDouble() - hauteurHudBlocsPx) / tailleCelluleBlocsPx).toInt()

    val jeuBlocs = JeuBlocs(
        colonnes = colonnesBlocs,
        lignes = lignesBlocs,
        taillePixel = tailleCelluleBlocsPx,
        hauteurPixel = hauteurHudBlocsPx,
    )

    val jeuSerpent = JeuSerpent(
        colonnes = colonnesSerpent,
        lignes = lignesSerpent,
        taillePixel = tailleCelluleSerpentPx,
        quandSerpentMangePoint = { jeuBlocs.ajouterPieceAleatoireDepuisSerpent() },
    )

    val controleur = ControleurTetrasnake(
        jeuSerpent = jeuSerpent,
        jeuBlocs = jeuBlocs,
        canvasSerpent = canvasSerpent,
        canvasBlocs = canvasBlocs,
        contexteSerpent = contexteSerpent,
        contexteBlocs = contexteBlocs,
        elementScoreSerpent = elementScoreSerpent,
        elementScoreBlocs = elementScoreBlocs,
        elementScoreTotal = elementScoreTotal,
        elementOverlay = elementOverlay,
        elementTexteGameOver = elementTexteGameOver,
        boutonRelancer = boutonRelancer,
        vitesse = vitesse,
    )
    controleur.demarrer()
}

private fun ajoutCanvas(id: String): HTMLCanvasElement {
    return document.getElementById(id) as? HTMLCanvasElement
        ?: error("Canvas introuvable: #$id")
}

private fun ajoutSpan(id: String): HTMLSpanElement {
    return document.getElementById(id) as? HTMLSpanElement
        ?: error("Span introuvable: #$id")
}

private fun ajoutDiv(id: String): HTMLDivElement {
    return document.getElementById(id) as? HTMLDivElement
        ?: error("Div introuvable: #$id")
}

private fun ajoutButton(id: String): HTMLButtonElement {
    return document.getElementById(id) as? HTMLButtonElement
        ?: error("Bouton introuvable: #$id")
}