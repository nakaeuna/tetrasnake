package taniere.tetrasnake

import org.w3c.dom.CanvasRenderingContext2D

private data class Block(val x: Int, val y: Int)

private data class PieceDef(val rotations: List<List<Block>>, val color: String)

private class Blocs {
    // Définition en coordonnées 4x4, rotation 0.
    // On génère ensuite les rotations par transformation matricielle.
    private fun definirPiece(rotation0: List<Block>, couleur: String): PieceDef {
        fun tourner90Degres(points: List<Block>): List<Block> = points.map { p ->
            // (x,y) -> (y, 3-x) dans une matrice 4x4
            Block(x = p.y, y = 3 - p.x)
        }

        val rotation1 = tourner90Degres(rotation0)
        val rotation2 = tourner90Degres(rotation1)
        val rotation3 = tourner90Degres(rotation2)
        return PieceDef(rotations = listOf(rotation0, rotation1, rotation2, rotation3), color = couleur)
    }
    
    // Définition des pièces du jeu de blocs (couleurs = [couleursPiecesHex], même ordre)
    val pieces: List<PieceDef> = listOf(
        definirPiece(
            rotation0 = listOf(Block(0, 1), Block(1, 1), Block(2, 1), Block(3, 1)),
            couleur = couleursPiecesHex[0],
        ),
        definirPiece(
            rotation0 = listOf(Block(1, 1), Block(2, 1), Block(1, 2), Block(2, 2)),
            couleur = couleursPiecesHex[1],
        ),
        definirPiece(
            rotation0 = listOf(Block(1, 0), Block(0, 1), Block(1, 1), Block(2, 1)),
            couleur = couleursPiecesHex[2],
        ),
        definirPiece(
            rotation0 = listOf(Block(0, 0), Block(0, 1), Block(1, 1), Block(2, 1)),
            couleur = couleursPiecesHex[3],
        ),
        definirPiece(
            rotation0 = listOf(Block(2, 0), Block(0, 1), Block(1, 1), Block(2, 1)),
            couleur = couleursPiecesHex[4],
        ),
        definirPiece(
            rotation0 = listOf(Block(1, 0), Block(2, 0), Block(0, 1), Block(1, 1)),
            couleur = couleursPiecesHex[5],
        ),
        definirPiece(
            rotation0 = listOf(Block(0, 0), Block(1, 0), Block(1, 1), Block(2, 1)),
            couleur = couleursPiecesHex[6],
        ),
    )
}

private data class BlocEnTrainDeTomber(
    val typeIndex: Int,
    var rotationIndex: Int,
    var posX: Int,
    var posY: Int,
)

private data class AffichageFuturBloc(
    val typeIndex: Int,
    val rotationIndex: Int,
)

class JeuBlocs(
    private val colonnes: Int,
    private val lignes: Int,
    private val taillePixel: Double,
    private val hauteurPixel: Double,
) {
    private val blocs = Blocs()

    // 0 = vide, sinon = index de couleur+1
    private val tableau: Array<IntArray> = Array(lignes) { IntArray(colonnes) }

    private val queue: ArrayDeque<Int> = ArrayDeque()
    private var enTrainDeTomber: BlocEnTrainDeTomber? = null

    private var graviteEnCoursSeconde = 0.0
    private val graviteIntervalleSeconde = 0.55
    private var descenteAccellereeMaintenue: Boolean = false

    var perdu: Boolean = false
        private set
    var score: Int = 0
        private set
    var lignesCompletees: Int = 0
        private set

    fun ajouterPieceDepuisIndiceType(indiceType: Int) {
        if (perdu) return
        val borne = indiceType.coerceIn(0, blocs.pieces.size - 1)
        queue.addLast(borne)
    }

    fun reinitialiser() {
        for (y in 0 until lignes) {
            for (x in 0 until colonnes) tableau[y][x] = 0
        }
        queue.clear()
        enTrainDeTomber = null
        graviteEnCoursSeconde = 0.0
        descenteAccellereeMaintenue = false
        perdu = false
        score = 0
        lignesCompletees = 0
    }

    fun setDescenteAccelereeMaintenue(maintenue: Boolean) {
        descenteAccellereeMaintenue = maintenue
    }

    fun forcerFinDePartie() {
        perdu = true
    }

    fun gererDeplacementGaucheBlocs() {
        val pieceActive = enTrainDeTomber ?: return
        tenterDeplacement(pieceActive, dx = -1, dy = 0)
    }

    fun gererDeplacementDroiteBlocs() {
        val pieceActive = enTrainDeTomber ?: return
        tenterDeplacement(pieceActive, dx = 1, dy = 0)
    }

    fun gererRotationHoraireBlocs() {
        val pieceActive = enTrainDeTomber ?: return

        val rotationSuivante = (pieceActive.rotationIndex + 1) % 4

        essayerRotationAvecAcceleration(pieceActive, rotationSuivante)
    }

    fun mettreAJour(deltaSeconde: Double) {
        if (perdu) return

        if (enTrainDeTomber == null) {
            if (queue.isNotEmpty()) genererProchainePieceDepuisFile()
            return
        }

        val intervalSec = calculerIntervalleGraviteSec()
        graviteEnCoursSeconde += deltaSeconde

        while (graviteEnCoursSeconde >= intervalSec) {
            graviteEnCoursSeconde -= intervalSec
            val pieceActiveCourante = enTrainDeTomber ?: return

            val deplacementPossible = tenterDeplacement(pieceActiveCourante, dx = 0, dy = 1)
            if (deplacementPossible) continue

            verrouillerPieceActiveEtTraiter()
            return
        }
    }

    private fun genererProchainePieceDepuisFile() {
        val typeIndex = queue.removeFirst()
        val spawnX = (colonnes - 4) / 2
        val spawnY = -1

        val pieceActiveProchaine = BlocEnTrainDeTomber(typeIndex = typeIndex, rotationIndex = 0, posX = spawnX, posY = spawnY)
        if (entreCollision(typeIndex = pieceActiveProchaine.typeIndex, rotationIndex = pieceActiveProchaine.rotationIndex, posX = pieceActiveProchaine.posX, posY = pieceActiveProchaine.posY)) {
            perdu = true
            return
        }
        enTrainDeTomber = pieceActiveProchaine
    }

    private fun tenterDeplacement(pieceActive: BlocEnTrainDeTomber, dx: Int, dy: Int): Boolean {
        val nouvellePosX = pieceActive.posX + dx
        val nouvellePosY = pieceActive.posY + dy
        if (entreCollision(typeIndex = pieceActive.typeIndex, rotationIndex = pieceActive.rotationIndex, posX = nouvellePosX, posY = nouvellePosY)) {
            return false
        }
        pieceActive.posX = nouvellePosX
        pieceActive.posY = nouvellePosY
        return true
    }

    private fun entreCollision(typeIndex: Int, rotationIndex: Int, posX: Int, posY: Int): Boolean {
        val piece = blocs.pieces[typeIndex]
        val blocks = piece.rotations[rotationIndex]
        for (b in blocks) {
            val coordX = posX + b.x
            val coordY = posY + b.y
            if (coordX !in 0 until colonnes) return true
            if (coordY >= lignes) return true
            if (coordY >= 0 && tableau[coordY][coordX] != 0) return true
        }
        return false
    }

    private fun verrouillerPieceActive() {
        val pieceActive = enTrainDeTomber ?: return
        val piece = blocs.pieces[pieceActive.typeIndex]
        val blocks = piece.rotations[pieceActive.rotationIndex]

        val indexCouleurPlusUn = pieceActive.typeIndex + 1
        for (b in blocks) {
            val coordX = pieceActive.posX + b.x
            val coordY = pieceActive.posY + b.y
            if (coordY in 0 until lignes && coordX in 0 until colonnes) {
                tableau[coordY][coordX] = indexCouleurPlusUn
            } else if (coordY < 0) {
                // Si un bloc verrouille au-dessus du haut visible, c'est une fin de partie.
                perdu = true
            }
        }
    }

    private fun effacerLignesEtAjouterScore() {
        var lignesEffacees = 0
        var y = lignes - 1

        while (y >= 0) {
            if (!ligneEstPleine(y)) {
                y--
                continue
            }

            supprimerLigne(y)
            lignesEffacees++
        }

        if (lignesEffacees > 0) ajouterScorePourLignesEffacees(lignesEffacees)
    }

    private fun ligneEstPleine(ligneY: Int): Boolean {
        for (x in 0 until colonnes) {
            if (tableau[ligneY][x] == 0) return false
        }
        return true
    }

    private fun supprimerLigne(ligneY: Int) {
        for (yy in ligneY downTo 1) {
            tableau[yy] = tableau[yy - 1]
        }
        tableau[0] = IntArray(colonnes)
    }

    private fun ajouterScorePourLignesEffacees(lignesEffacees: Int) {
        val pointsGagnes = lignesEffacees * lignesEffacees * 100
        score += pointsGagnes
        lignesCompletees += lignesEffacees
    }

    fun dessiner(ctx: CanvasRenderingContext2D) {
        val boardOriginY = hauteurPixel
        dessinerFondEtHud(ctx)
        dessinerPieceSuivante(ctx)
        dessinerGrille(ctx, boardOriginY)
        dessinerBoardStatique(ctx, boardOriginY)
        dessinerPieceActive(ctx, boardOriginY)
    }

    private fun dessinerFondEtHud(ctx: CanvasRenderingContext2D) {
        // Fond complet.
        ctx.fillStyle = "rgba(11,18,32,1)"
        ctx.fillRect(0.0, 0.0, colonnes * taillePixel, hauteurPixel + lignes * taillePixel)

        // HUD en haut (suivante).
        ctx.fillStyle = "rgba(2, 6, 23, 0.75)"
        ctx.fillRect(0.0, 0.0, colonnes * taillePixel, hauteurPixel)

        ctx.strokeStyle = "rgba(255,255,255,0.12)"
        ctx.lineWidth = 2.0
        ctx.strokeRect(8.0, 8.0, colonnes * taillePixel - 16.0, hauteurPixel - 16.0)

        ctx.fillStyle = "#E5E7EB"
        ctx.font = "bold 16px Inter, Arial, sans-serif"
        ctx.fillText("NEXT", 18.0, 30.0)
    }

    private data class BoitePreview(
        val x: Double,
        val y: Double,
        val largeur: Double,
        val hauteur: Double,
    )

    private fun calculerBoitePieceSuivante(): BoitePreview {
        val previewBoxW = 140.0
        val previewBoxH = hauteurPixel - 44.0
        val previewBoxX = colonnes * taillePixel - 18.0 - previewBoxW
        val previewBoxY = 34.0
        return BoitePreview(x = previewBoxX, y = previewBoxY, largeur = previewBoxW, hauteur = previewBoxH)
    }

    private fun obtenirPieceSuivante(): AffichageFuturBloc? {
        return when {
            queue.isNotEmpty() -> AffichageFuturBloc(typeIndex = queue.first(), rotationIndex = 0)
            enTrainDeTomber != null -> AffichageFuturBloc(typeIndex = enTrainDeTomber!!.typeIndex, rotationIndex = enTrainDeTomber!!.rotationIndex)
            else -> null
        }
    }

    private fun dessinerPieceSuivante(ctx: CanvasRenderingContext2D) {
        val boite = calculerBoitePieceSuivante()

        ctx.fillStyle = "rgba(255,255,255,0.04)"
        ctx.fillRect(boite.x, boite.y, boite.largeur, boite.hauteur)
        ctx.strokeStyle = "rgba(34, 211, 238, 0.55)"
        ctx.lineWidth = 2.0
        ctx.strokeRect(boite.x, boite.y, boite.largeur, boite.hauteur)

        val pieceSuivante = obtenirPieceSuivante() ?: return

        val piece = blocs.pieces[pieceSuivante.typeIndex]
        val blocs = piece.rotations[pieceSuivante.rotationIndex]

        val previewCell = (minOf(boite.largeur, boite.hauteur) / 4.0).coerceAtLeast(2.0)
        val utiliseW = previewCell * 4.0
        val utiliseH = previewCell * 4.0
        val offsetX = boite.x + (boite.largeur - utiliseW) / 2.0
        val offsetY = boite.y + (boite.hauteur - utiliseH) / 2.0

        val m = previewCell * 0.12
        for (bloc in blocs) {
            ctx.fillStyle = piece.color
            ctx.fillRect(
                offsetX + bloc.x * previewCell + m,
                offsetY + bloc.y * previewCell + m,
                previewCell - 2 * m,
                previewCell - 2 * m,
            )
        }
    }

    private fun dessinerGrille(ctx: CanvasRenderingContext2D, boardOriginY: Double) {
        ctx.strokeStyle = "rgba(255,255,255,0.04)"
        ctx.lineWidth = 1.0

        for (x in 0 until colonnes) {
            val px = x * taillePixel
            ctx.beginPath()
            ctx.moveTo(px, boardOriginY)
            ctx.lineTo(px, boardOriginY + lignes * taillePixel)
            ctx.stroke()
        }

        for (y in 0 until lignes) {
            val py = boardOriginY + y * taillePixel
            ctx.beginPath()
            ctx.moveTo(0.0, py)
            ctx.lineTo(colonnes * taillePixel, py)
            ctx.stroke()
        }
    }

    private fun dessinerCelluleBoardStatique(ctx: CanvasRenderingContext2D, x: Int, y: Int, boardOriginY: Double) {
        val indexCouleurPlusUn = tableau[y][x]
        if (indexCouleurPlusUn == 0) return
        val indiceType = indexCouleurPlusUn - 1
        val couleur = blocs.pieces[indiceType].color
        dessinerBloc(ctx, x, y, couleur, boardOriginY)
    }

    private fun dessinerBoardStatique(ctx: CanvasRenderingContext2D, boardOriginY: Double) {
        for (y in 0 until lignes) {
            for (x in 0 until colonnes) {
                dessinerCelluleBoardStatique(ctx, x, y, boardOriginY)
            }
        }
    }

    private fun dessinerPieceActive(ctx: CanvasRenderingContext2D, boardOriginY: Double) {
        val piece = enTrainDeTomber ?: return
        val definition = blocs.pieces[piece.typeIndex]
        val blocs = definition.rotations[piece.rotationIndex]
        val couleur = definition.color

        for (bloc in blocs) {
            val coordX = piece.posX + bloc.x
            val coordY = piece.posY + bloc.y
            if (coordY >= 0) dessinerBloc(ctx, coordX, coordY, couleur, boardOriginY)
        }
    }

    private fun dessinerBloc(
        ctx: CanvasRenderingContext2D,
        x: Int,
        y: Int,
        color: String,
        boardOriginY: Double,
    ) {
        val px = x * taillePixel
        val py = boardOriginY + y * taillePixel
        val m = taillePixel * 0.10
        ctx.fillStyle = color
        ctx.fillRect(px + m, py + m, taillePixel - 2 * m, taillePixel - 2 * m)
    }

    private fun calculerIntervalleGraviteSec(): Double {
        return if (descenteAccellereeMaintenue) graviteIntervalleSeconde / 12.0 else graviteIntervalleSeconde
    }

    private fun verrouillerPieceActiveEtTraiter() {
        verrouillerPieceActive()
        enTrainDeTomber = null
        effacerLignesEtAjouterScore()
        if (perdu) return
        if (queue.isNotEmpty()) genererProchainePieceDepuisFile()
    }

    private fun essayerRotationAvecAcceleration(pieceActive: BlocEnTrainDeTomber, rotationSuivante: Int) {
        val candidates = listOf(
            Pair(0, 0),
            Pair(-1, 0),
            Pair(1, 0),
            Pair(-2, 0),
            Pair(2, 0),
            Pair(0, -1),
            Pair(-1, -1),
            Pair(1, -1),
        )

        for ((decalageX, decalageY) in candidates) {
            val collision = entreCollision(
                typeIndex = pieceActive.typeIndex,
                rotationIndex = rotationSuivante,
                posX = pieceActive.posX + decalageX,
                posY = pieceActive.posY + decalageY,
            )
            if (collision) continue

            pieceActive.rotationIndex = rotationSuivante
            pieceActive.posX += decalageX
            pieceActive.posY += decalageY
            return
        }
    }
}

