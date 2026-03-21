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
    val hauteurHudBlocsPx = 80.0
    val hHudEntier = hauteurHudBlocsPx.toInt()
    val (tailleCelluleBlocsPx, lignesBlocs) = dimensionsGrilleBlocs(
        largeurCanvas = canvasBlocs.width,
        hauteurCanvas = canvasBlocs.height,
        colonnes = colonnesBlocs,
        hauteurHudPx = hHudEntier,
        canvasBlocs = canvasBlocs,
    )

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
        quandSerpentMangePoint = { indiceTypePiece ->
            jeuBlocs.ajouterPieceDepuisIndiceType(indiceTypePiece)
        },
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

/**
 * Cellules carrées : taille = largeurCanvas / colonnes (entier).
 * On ajuste la hauteur du canvas pour que (hauteur - HUD) soit un multiple exact de cette taille,
 * sinon une bande vide en bas faisait croire que la « première » ligne (vue du bas) ne se remplissait pas.
 */
private fun dimensionsGrilleBlocs(
    largeurCanvas: Int,
    hauteurCanvas: Int,
    colonnes: Int,
    hauteurHudPx: Int,
    canvasBlocs: HTMLCanvasElement,
): Pair<Double, Int> {
    val tailleCellule = largeurCanvas / colonnes
    require(tailleCellule > 0) { "Canvas blocs trop étroit pour $colonnes colonnes." }

    val hauteurZone = hauteurCanvas - hauteurHudPx
    val lignes = hauteurZone / tailleCellule
    val hauteurCible = hauteurHudPx + lignes * tailleCellule

    if (hauteurCible != hauteurCanvas) {
        canvasBlocs.height = hauteurCible
    }

    return tailleCellule.toDouble() to lignes
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