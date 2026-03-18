package taniere.tetrasnake

import org.w3c.dom.CanvasRenderingContext2D
import kotlin.random.Random

private data class Cell(val x: Int, val y: Int)

private enum class Direction(val dx: Int, val dy: Int) {
    Up(0, -1),
    Right(1, 0),
    Down(0, 1),
    Left(-1, 0),
}

private fun isOpposite(a: Direction, b: Direction): Boolean = a.dx == -b.dx && a.dy == -b.dy

class SnakeGame(
    private val cols: Int,
    private val rows: Int,
    private val cellSizePx: Double,
    private val onSnakeEatsPoint: () -> Unit,
) {
    private val rng = Random.Default

    private val snake = ArrayDeque<Cell>()
    private var direction: Direction = Direction.Right
    private var queuedDirection: Direction = direction
    private var food: Cell = Cell(0, 0)

    private var moveAccSec = 0.0
    private val moveIntervalSec = 0.10

    var gameOver: Boolean = false
        private set
    var score: Int = 0
        private set

    init {
        reset()
    }

    fun reset() {
        snake.clear()
        val startX = cols / 2
        val startY = rows / 2
        snake.addLast(Cell(startX - 1, startY))
        snake.addLast(Cell(startX, startY))
        snake.addLast(Cell(startX + 1, startY))

        direction = Direction.Right
        queuedDirection = direction
        gameOver = false
        score = 0
        moveAccSec = 0.0

        food = randomEmptyCell()
    }

    fun handleLeftUp() {
        if (gameOver) return
        val newDir = Direction.Up
        if (!isOpposite(direction, newDir)) queuedDirection = newDir
    }

    fun handleLeftLeft() {
        if (gameOver) return
        val newDir = Direction.Left
        if (!isOpposite(direction, newDir)) queuedDirection = newDir
    }

    fun handleLeftDown() {
        if (gameOver) return
        val newDir = Direction.Down
        if (!isOpposite(direction, newDir)) queuedDirection = newDir
    }

    fun handleLeftRight() {
        if (gameOver) return
        val newDir = Direction.Right
        if (!isOpposite(direction, newDir)) queuedDirection = newDir
    }

    fun update(deltaSec: Double) {
        if (gameOver) return

        moveAccSec += deltaSec
        while (moveAccSec >= moveIntervalSec) {
            moveAccSec -= moveIntervalSec

            direction = queuedDirection
            val head = snake.last()
            val next = Cell(head.x + direction.dx, head.y + direction.dy)

            val willEat = next == food

            // Murs = game over (pas de wrap).
            if (next.x !in 0 until cols || next.y !in 0 until rows) {
                gameOver = true
                return
            }

            // Collisions avec le corps (si on ne mange pas, la queue bouge).
            val tail = snake.first()
            val collidesWithBody = if (!willEat && next == tail) {
                false
            } else {
                snake.any { it == next }
            }
            if (collidesWithBody) {
                gameOver = true
                return
            }

            snake.addLast(next)
            if (willEat) {
                score++
                onSnakeEatsPoint()
                food = randomEmptyCell()
                if (snake.size >= cols * rows) {
                    // Grille entièrement remplie: victoire implicite.
                    gameOver = true
                }
            } else {
                snake.removeFirst()
            }
        }
    }

    fun forceGameOver() {
        gameOver = true
    }

    private fun randomEmptyCell(): Cell {
        if (snake.size >= cols * rows) return Cell(0, 0)
        while (true) {
            val c = Cell(rng.nextInt(cols), rng.nextInt(rows))
            if (!snake.any { it == c }) return c
        }
    }

    fun draw(ctx: CanvasRenderingContext2D) {
        // Grille légère.
        ctx.strokeStyle = "rgba(255,255,255,0.04)"
        ctx.lineWidth = 1.0
        for (x in 0 until cols) {
            val px = x * cellSizePx
            ctx.beginPath()
            ctx.moveTo(px, 0.0)
            ctx.lineTo(px, rows * cellSizePx)
            ctx.stroke()
        }
        for (y in 0 until rows) {
            val py = y * cellSizePx
            ctx.beginPath()
            ctx.moveTo(0.0, py)
            ctx.lineTo(cols * cellSizePx, py)
            ctx.stroke()
        }

        // Food.
        val margin = cellSizePx * 0.2
        ctx.fillStyle = "#FDE047"
        ctx.fillRect(
            food.x * cellSizePx + margin,
            food.y * cellSizePx + margin,
            cellSizePx - 2 * margin,
            cellSizePx - 2 * margin,
        )

        // Snake.
        for ((index, cell) in snake.withIndex()) {
            val isHead = index == snake.size - 1
            ctx.fillStyle = if (isHead) "#22C55E" else "#16A34A"
            val m = cellSizePx * 0.18
            ctx.fillRect(
                cell.x * cellSizePx + m,
                cell.y * cellSizePx + m,
                cellSizePx - 2 * m,
                cellSizePx - 2 * m,
            )
        }
    }
}

