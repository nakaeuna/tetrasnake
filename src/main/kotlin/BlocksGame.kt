package taniere.tetrasnake

import org.w3c.dom.CanvasRenderingContext2D
import kotlin.random.Random

private data class Block(val x: Int, val y: Int)

private data class PieceDef(val rotations: List<List<Block>>, val color: String)

private class Tetrominoes {
    // Définition en coordonnées 4x4, rotation 0.
    // On génère ensuite les rotations par transform matricielle.
    private fun mk(rot0: List<Block>, color: String): PieceDef {
        fun rot90(points: List<Block>): List<Block> = points.map { p ->
            // (x,y) -> (y, 3-x) dans une matrice 4x4
            Block(x = p.y, y = 3 - p.x)
        }

        val r0 = rot0
        val r1 = rot90(r0)
        val r2 = rot90(r1)
        val r3 = rot90(r2)
        return PieceDef(rotations = listOf(r0, r1, r2, r3), color = color)
    }

    val pieces: List<PieceDef> = listOf(
        // I
        mk(
            rot0 = listOf(Block(0, 1), Block(1, 1), Block(2, 1), Block(3, 1)),
            color = "#22D3EE",
        ),
        // O
        mk(
            rot0 = listOf(Block(1, 1), Block(2, 1), Block(1, 2), Block(2, 2)),
            color = "#F59E0B",
        ),
        // T
        mk(
            rot0 = listOf(Block(1, 0), Block(0, 1), Block(1, 1), Block(2, 1)),
            color = "#A78BFA",
        ),
        // J
        mk(
            rot0 = listOf(Block(0, 0), Block(0, 1), Block(1, 1), Block(2, 1)),
            color = "#60A5FA",
        ),
        // L
        mk(
            rot0 = listOf(Block(2, 0), Block(0, 1), Block(1, 1), Block(2, 1)),
            color = "#F97316",
        ),
        // S
        mk(
            rot0 = listOf(Block(1, 0), Block(2, 0), Block(0, 1), Block(1, 1)),
            color = "#34D399",
        ),
        // Z
        mk(
            rot0 = listOf(Block(0, 0), Block(1, 0), Block(1, 1), Block(2, 1)),
            color = "#EF4444",
        ),
    )
}

private data class ActivePiece(
    val typeIndex: Int,
    var rotationIndex: Int,
    var posX: Int,
    var posY: Int,
)

private data class PreviewPiece(
    val typeIndex: Int,
    val rotationIndex: Int,
)

class BlocksGame(
    private val cols: Int,
    private val rows: Int,
    private val cellSizePx: Double,
    private val hudHeightPx: Double,
) {
    private val tetrominoes = Tetrominoes()
    private val rng = Random.Default

    // 0 = vide, sinon = index de couleur+1
    private val board: Array<IntArray> = Array(rows) { IntArray(cols) }

    private val queue: ArrayDeque<Int> = ArrayDeque()
    private var active: ActivePiece? = null

    private var gravityAccSec = 0.0
    private val gravityIntervalSec = 0.55
    private var downHeld: Boolean = false

    var gameOver: Boolean = false
        private set
    var score: Int = 0
        private set
    var linesClearedTotal: Int = 0
        private set

    fun enqueueRandomPieceFromSnake() {
        if (gameOver) return
        queue.addLast(rng.nextInt(tetrominoes.pieces.size))
    }

    fun reset() {
        for (y in 0 until rows) {
            for (x in 0 until cols) board[y][x] = 0
        }
        queue.clear()
        active = null
        gravityAccSec = 0.0
        downHeld = false
        gameOver = false
        score = 0
        linesClearedTotal = 0
    }

    fun setDownHeld(held: Boolean) {
        downHeld = held
    }

    fun forceGameOver() {
        gameOver = true
    }

    fun handleMoveLeft() {
        val a = active ?: return
        tryMove(a, dx = -1, dy = 0)
    }

    fun handleMoveRight() {
        val a = active ?: return
        tryMove(a, dx = 1, dy = 0)
    }

    fun handleRotateClockwise() {
        val a = active ?: return

        val nextRot = (a.rotationIndex + 1) % 4

        // Mini wall-kicks simples: on tente quelques décalages.
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
        for ((dx, dy) in candidates) {
            if (wouldCollide(
                    typeIndex = a.typeIndex,
                    rotationIndex = nextRot,
                    posX = a.posX + dx,
                    posY = a.posY + dy,
                ).not()
            ) {
                a.rotationIndex = nextRot
                a.posX += dx
                a.posY += dy
                return
            }
        }
    }

    fun update(deltaSec: Double) {
        if (gameOver) return

        if (active == null) {
            if (queue.isNotEmpty()) spawnNextFromQueue()
            return
        }

        val interval = if (downHeld) gravityIntervalSec / 12.0 else gravityIntervalSec
        gravityAccSec += deltaSec

        while (gravityAccSec >= interval) {
            gravityAccSec -= interval
            val a = active ?: return
            if (!tryMove(a, dx = 0, dy = 1)) {
                lockActivePiece()
                active = null
                clearLinesAndScore()
                if (gameOver) return
                if (queue.isNotEmpty()) spawnNextFromQueue()
                return
            }
        }
    }

    private fun spawnNextFromQueue() {
        val typeIndex = queue.removeFirst()
        // Centrage approximatif sur une matrice 4x4.
        val spawnX = (cols - 4) / 2
        val spawnY = -1

        val a = ActivePiece(typeIndex = typeIndex, rotationIndex = 0, posX = spawnX, posY = spawnY)
        if (wouldCollide(typeIndex = a.typeIndex, rotationIndex = a.rotationIndex, posX = a.posX, posY = a.posY)) {
            gameOver = true
            return
        }
        active = a
    }

    private fun tryMove(a: ActivePiece, dx: Int, dy: Int): Boolean {
        val newX = a.posX + dx
        val newY = a.posY + dy
        if (wouldCollide(typeIndex = a.typeIndex, rotationIndex = a.rotationIndex, posX = newX, posY = newY)) {
            return false
        }
        a.posX = newX
        a.posY = newY
        return true
    }

    private fun wouldCollide(typeIndex: Int, rotationIndex: Int, posX: Int, posY: Int): Boolean {
        val piece = tetrominoes.pieces[typeIndex]
        val blocks = piece.rotations[rotationIndex]
        for (b in blocks) {
            val bx = posX + b.x
            val by = posY + b.y
            if (bx !in 0 until cols) return true
            if (by >= rows) return true
            if (by >= 0) {
                if (board[by][bx] != 0) return true
            }
        }
        return false
    }

    private fun lockActivePiece() {
        val a = active ?: return
        val piece = tetrominoes.pieces[a.typeIndex]
        val blocks = piece.rotations[a.rotationIndex]

        val colorIndexPlusOne = a.typeIndex + 1
        for (b in blocks) {
            val bx = a.posX + b.x
            val by = a.posY + b.y
            if (by in 0 until rows && bx in 0 until cols) {
                board[by][bx] = colorIndexPlusOne
            } else if (by < 0) {
                // Si un bloc verrouille au-dessus du haut visible, c'est un game over.
                gameOver = true
            }
        }
    }

    private fun clearLinesAndScore() {
        var cleared = 0
        var y = rows - 1
        while (y >= 0) {
            var full = true
            for (x in 0 until cols) {
                if (board[y][x] == 0) {
                    full = false
                    break
                }
            }
            if (full) {
                // Supprime la ligne y et remonte tout.
                for (yy in y downTo 1) {
                    board[yy] = board[yy - 1]
                }
                board[0] = IntArray(cols)
                cleared++
            } else {
                y--
            }
        }

        if (cleared > 0) {
            val gained = cleared * cleared * 100
            score += gained
            linesClearedTotal += cleared
        }
    }

    fun draw(ctx: CanvasRenderingContext2D) {
        val boardOriginY = hudHeightPx

        // Fond complet.
        ctx.fillStyle = "rgba(11,18,32,1)"
        ctx.fillRect(0.0, 0.0, cols * cellSizePx, hudHeightPx + rows * cellSizePx)

        // HUD en haut (NEXT).
        ctx.fillStyle = "rgba(2, 6, 23, 0.75)"
        ctx.fillRect(0.0, 0.0, cols * cellSizePx, hudHeightPx)

        ctx.strokeStyle = "rgba(255,255,255,0.12)"
        ctx.lineWidth = 2.0
        ctx.strokeRect(8.0, 8.0, cols * cellSizePx - 16.0, hudHeightPx - 16.0)

        ctx.fillStyle = "#E5E7EB"
        ctx.font = "bold 16px Inter, Arial, sans-serif"
        ctx.fillText("NEXT", 18.0, 30.0)

        // Encadré "pièce suivante".
        val previewBoxW = 140.0
        val previewBoxH = hudHeightPx - 44.0
        val previewBoxX = cols * cellSizePx - 18.0 - previewBoxW
        val previewBoxY = 34.0

        ctx.fillStyle = "rgba(255,255,255,0.04)"
        ctx.fillRect(previewBoxX, previewBoxY, previewBoxW, previewBoxH)
        ctx.strokeStyle = "rgba(34, 211, 238, 0.55)"
        ctx.lineWidth = 2.0
        ctx.strokeRect(previewBoxX, previewBoxY, previewBoxW, previewBoxH)

        val previewPiece = when {
            queue.isNotEmpty() -> {
                val typeIndex = queue.first()
                PreviewPiece(typeIndex = typeIndex, rotationIndex = 0)
            }
            active != null -> {
                PreviewPiece(typeIndex = active!!.typeIndex, rotationIndex = active!!.rotationIndex)
            }
            else -> null
        }

        if (previewPiece != null) {
            val piece = tetrominoes.pieces[previewPiece.typeIndex]
            val blocks = piece.rotations[previewPiece.rotationIndex]
            val previewCell = (minOf(previewBoxW, previewBoxH) / 4.0).coerceAtLeast(2.0)
            val usedW = previewCell * 4.0
            val usedH = previewCell * 4.0
            val offsetX = previewBoxX + (previewBoxW - usedW) / 2.0
            val offsetY = previewBoxY + (previewBoxH - usedH) / 2.0

            for (b in blocks) {
                ctx.fillStyle = piece.color
                val m = previewCell * 0.12
                ctx.fillRect(
                    offsetX + b.x * previewCell + m,
                    offsetY + b.y * previewCell + m,
                    previewCell - 2 * m,
                    previewCell - 2 * m,
                )
            }
        }

        // Grille du board.
        ctx.strokeStyle = "rgba(255,255,255,0.04)"
        ctx.lineWidth = 1.0
        for (x in 0 until cols) {
            val px = x * cellSizePx
            ctx.beginPath()
            ctx.moveTo(px, boardOriginY)
            ctx.lineTo(px, boardOriginY + rows * cellSizePx)
            ctx.stroke()
        }
        for (y in 0 until rows) {
            val py = boardOriginY + y * cellSizePx
            ctx.beginPath()
            ctx.moveTo(0.0, py)
            ctx.lineTo(cols * cellSizePx, py)
            ctx.stroke()
        }

        // Board statique.
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                val colorIndexPlusOne = board[y][x]
                if (colorIndexPlusOne == 0) continue
                val typeIndex = colorIndexPlusOne - 1
                val color = tetrominoes.pieces[typeIndex].color
                drawBlock(ctx, x, y, color, boardOriginY)
            }
        }

        // Pièce active.
        val a = active
        if (a != null) {
            val piece = tetrominoes.pieces[a.typeIndex]
            val blocks = piece.rotations[a.rotationIndex]
            val color = piece.color
            for (b in blocks) {
                val bx = a.posX + b.x
                val by = a.posY + b.y
                if (by >= 0) drawBlock(ctx, bx, by, color, boardOriginY)
            }
        }
    }

    private fun drawBlock(
        ctx: CanvasRenderingContext2D,
        x: Int,
        y: Int,
        color: String,
        boardOriginY: Double,
    ) {
        val px = x * cellSizePx
        val py = boardOriginY + y * cellSizePx
        val m = cellSizePx * 0.10
        ctx.fillStyle = color
        ctx.fillRect(px + m, py + m, cellSizePx - 2 * m, cellSizePx - 2 * m)
    }
}

