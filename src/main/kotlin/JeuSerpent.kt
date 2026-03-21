package taniere.tetrasnake

import org.w3c.dom.CanvasRenderingContext2D
import kotlin.random.Random

private data class Pixels(val x: Int, val y: Int)

private enum class Direction(val dx: Int, val dy: Int) {
    Haut(0, -1),
    Droite(1, 0),
    Bas(0, 1),
    Gauche(-1, 0),
}

private fun directionsSontOpposees(a: Direction, b: Direction): Boolean = a.dx == -b.dx && a.dy == -b.dy

class JeuSerpent(
    private val colonnes: Int,
    private val lignes: Int,
    private val taillePixel: Double,
    /** Indice du type de pièce (0..6), aligné sur [couleursPiecesHex]. */
    private val quandSerpentMangePoint: (Int) -> Unit,
) {
    private val alea = Random.Default

    private val serpent = ArrayDeque<Pixels>()
    private var directionCourante: Direction = Direction.Droite
    private var directionProchaine: Direction = directionCourante
    private var nourriture: Pixels = Pixels(0, 0)
    private var indiceTypeNourriture: Int = 0

    private var accumulateurDeplacementSeconde = 0.0
    private val intervalleDeplacementSeconde = 0.10

    var perdu: Boolean = false
        private set
    var score: Int = 0
        private set

    init {
        reinitialiser()
    }

    fun reinitialiser() {
        serpent.clear()
        val departX = colonnes / 2
        val departY = lignes / 2
        serpent.addLast(Pixels(departX - 1, departY))
        serpent.addLast(Pixels(departX, departY))
        serpent.addLast(Pixels(departX + 1, departY))

        directionCourante = Direction.Droite
        directionProchaine = directionCourante
        perdu = false
        score = 0
        accumulateurDeplacementSeconde = 0.0

        placerNouvelleNourriture()
    }

    fun gererDeplacementHautSerpent() {
        tenterChangerDirection(Direction.Haut)
    }

    fun gererDeplacementGaucheSerpent() {
        tenterChangerDirection(Direction.Gauche)
    }

    fun gererDeplacementBasSerpent() {
        tenterChangerDirection(Direction.Bas)
    }

    fun gererDeplacementDroiteSerpent() {
        tenterChangerDirection(Direction.Droite)
    }

    private fun tenterChangerDirection(nouvelle: Direction) {
        if (perdu) return
        if (!peutChoisirDirection(nouvelle)) return
        directionProchaine = nouvelle
    }

    /** Refuse 180° et tout déplacement qui ferait entrer la tête dans le corps. */
    private fun peutChoisirDirection(nouvelle: Direction): Boolean {
        if (directionsSontOpposees(directionCourante, nouvelle)) return false
        val tete = serpent.last()
        val cible = Pixels(tete.x + nouvelle.dx, tete.y + nouvelle.dy)
        val mangera = cible == nourriture
        return !mordDansCorps(cible, mangera)
    }

    fun mettreAJour(deltaSec: Double) {
        if (perdu) return

        accumulateurDeplacementSeconde += deltaSec
        while (accumulateurDeplacementSeconde >= intervalleDeplacementSeconde) {
            accumulateurDeplacementSeconde -= intervalleDeplacementSeconde

            actualiserDirectionCourante()
            val teteCourante = serpent.last()
            val prochaineCase = calculerProchaineCase(teteCourante)
            val vaManger = prochaineCase == nourriture

            if (!caseDansGrille(prochaineCase)) {
                perdu = true
                return
            }

            if (mordDansCorps(prochaineCase, vaManger)) {
                perdu = true
                return
            }

            avancerSerpent(prochaineCase, vaManger)
        }
    }

    fun forcerFinDePartie() {
        perdu = true
    }

    private fun actualiserDirectionCourante() {
        directionCourante = directionProchaine
    }

    private fun calculerProchaineCase(tete: Pixels): Pixels {
        return Pixels(
            tete.x + directionCourante.dx,
            tete.y + directionCourante.dy,
        )
    }

    private fun caseDansGrille(cible: Pixels): Boolean {
        return cible.x in 0 until colonnes && cible.y in 0 until lignes
    }

    /**
     * Vrai si la tête irait sur une case du corps (interdit).
     * La queue actuelle est libérée au prochain pas si on ne mange pas.
     */
    private fun mordDansCorps(cible: Pixels, vaManger: Boolean): Boolean {
        if (vaManger) return false
        if (serpent.isEmpty()) return false
        val queue = serpent.first()
        if (cible == queue) return false
        for (segment in serpent.dropLast(1)) {
            if (segment == cible) return true
        }
        return false
    }

    private fun avancerSerpent(cible: Pixels, vaManger: Boolean) {
        serpent.addLast(cible)

        if (!vaManger) {
            serpent.removeFirst()
            return
        }

        score++
        quandSerpentMangePoint(indiceTypeNourriture)
        placerNouvelleNourriture()

        if (serpent.size >= colonnes * lignes) {
            // Grille entièrement remplie : victoire implicite.
            perdu = true
        }
    }

    private fun placerNouvelleNourriture() {
        indiceTypeNourriture = alea.nextInt(couleursPiecesHex.size)
        nourriture = randomCelluleVideSansSerpent()
    }

    private fun randomCelluleVideSansSerpent(): Pixels {
        if (serpent.size >= colonnes * lignes) return Pixels(0, 0)
        while (true) {
            val c = Pixels(alea.nextInt(colonnes), alea.nextInt(lignes))
            if (!serpent.any { it == c }) return c
        }
    }

    fun dessiner(ctx: CanvasRenderingContext2D) {
        dessinerGrille(ctx)
        dessinerNourriture(ctx)
        dessinerSerpent(ctx)
    }

    private fun dessinerGrille(ctx: CanvasRenderingContext2D) {
        ctx.strokeStyle = "rgba(255,255,255,0.04)"
        ctx.lineWidth = 1.0

        for (x in 0 until colonnes) {
            val px = x * taillePixel
            ctx.beginPath()
            ctx.moveTo(px, 0.0)
            ctx.lineTo(px, lignes * taillePixel)
            ctx.stroke()
        }

        for (y in 0 until lignes) {
            val py = y * taillePixel
            ctx.beginPath()
            ctx.moveTo(0.0, py)
            ctx.lineTo(colonnes * taillePixel, py)
            ctx.stroke()
        }
    }

    private fun dessinerNourriture(ctx: CanvasRenderingContext2D) {
        val marge = taillePixel * 0.2
        ctx.fillStyle = couleursPiecesHex[indiceTypeNourriture.coerceIn(0, couleursPiecesHex.size - 1)]
        ctx.fillRect(
            nourriture.x * taillePixel + marge,
            nourriture.y * taillePixel + marge,
            taillePixel - 2 * marge,
            taillePixel - 2 * marge,
        )
    }

    private fun dessinerSerpent(ctx: CanvasRenderingContext2D) {
        for ((index, cellule) in serpent.withIndex()) {
            val estTete = index == serpent.size - 1
            ctx.fillStyle = if (estTete) "#22C55E" else "#16A34A"
            val m = taillePixel * 0.18
            ctx.fillRect(
                cellule.x * taillePixel + m,
                cellule.y * taillePixel + m,
                taillePixel - 2 * m,
                taillePixel - 2 * m,
            )
        }
    }
}

