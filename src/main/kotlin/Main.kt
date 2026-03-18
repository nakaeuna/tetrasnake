package taniere.tetrasnake

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLCanvasElement

fun main() {
    val snakeCanvas = requireCanvas("canvas-snake")
    val blocksCanvas = requireCanvas("canvas-blocks")

    val snakeCtx = snakeCanvas.getContext("2d") as CanvasRenderingContext2D
    val blocksCtx = blocksCanvas.getContext("2d") as CanvasRenderingContext2D

    val scoreSnakeEl = requireSpan("score-snake")
    val scoreTetrisEl = requireSpan("score-tetris")
    val scoreTotalEl = requireSpan("score-total")
    val overlayEl = requireDiv("game-overlay")
    val gameOverTextEl = requireDiv("game-over-text")
    val restartBtn = requireButton("restart-btn")

    data class SpeedState(var snakeMul: Double, var blocksMul: Double)
    val speed = SpeedState(snakeMul = 1.0, blocksMul = 1.0)

    var generalGameOver = false

    val snakeCellSizePx = 20.0
    val snakeCols = (snakeCanvas.width / snakeCellSizePx).toInt()
    val snakeRows = (snakeCanvas.height / snakeCellSizePx).toInt()

    val blocksCols = 10
    val blocksCellSizePx = blocksCanvas.width.toDouble() / blocksCols.toDouble()
    val blocksHudHeightPx = 80.0
    val blocksRows = ((blocksCanvas.height.toDouble() - blocksHudHeightPx) / blocksCellSizePx).toInt()

    val blocksGame = BlocksGame(
        cols = blocksCols,
        rows = blocksRows,
        cellSizePx = blocksCellSizePx,
        hudHeightPx = blocksHudHeightPx,
    )

    val snakeGame = SnakeGame(
        cols = snakeCols,
        rows = snakeRows,
        cellSizePx = snakeCellSizePx,
        onSnakeEatsPoint = { blocksGame.enqueueRandomPieceFromSnake() },
    )

    fun restartAll() {
        snakeGame.reset()
        blocksGame.reset()
        blocksGame.setDownHeld(false)
        generalGameOver = false
        overlayEl.classList.add("hidden")
    }

    val lastActionMs = mutableMapOf<String, Double>()
    fun canFire(action: String, minGapMs: Double): Boolean {
        val now = window.performance.now()
        val last = lastActionMs[action] ?: Double.NEGATIVE_INFINITY
        if (now - last < minGapMs) return false
        lastActionMs[action] = now
        return true
    }

    window.addEventListener("keydown", { e ->
        val event = e as KeyboardEvent
        val key = event.key

        // Restart.
        if (key.equals("r", ignoreCase = true)) {
            restartAll()
            return@addEventListener
        }

        if (generalGameOver) return@addEventListener

        when (key) {
            "z", "Z" -> {
                if (canFire("snake-up", 40.0)) snakeGame.handleLeftUp()
            }
            "q", "Q" -> {
                if (canFire("snake-left", 40.0)) snakeGame.handleLeftLeft()
            }
            "s", "S" -> {
                if (canFire("snake-down", 40.0)) snakeGame.handleLeftDown()
            }
            "d", "D" -> {
                if (canFire("snake-right", 40.0)) snakeGame.handleLeftRight()
            }
            "ArrowLeft" -> {
                event.preventDefault()
                if (canFire("tetris-left", 40.0)) blocksGame.handleMoveLeft()
            }
            "ArrowRight" -> {
                event.preventDefault()
                if (canFire("tetris-right", 40.0)) blocksGame.handleMoveRight()
            }
            "ArrowUp" -> {
                event.preventDefault()
                if (canFire("tetris-rotate", 80.0)) blocksGame.handleRotateClockwise()
            }
            "ArrowDown" -> {
                event.preventDefault()
                blocksGame.setDownHeld(true)
                // Un petit "soft drop" immédiat pour réactivité.
                if (canFire("tetris-down-step", 60.0)) {
                    // On simule un tick en demandant une descente plus fréquente (le held le fera de toute façon).
                }
            }
        }
    })

    restartBtn.addEventListener("click", {
        restartAll()
    })

    window.addEventListener("keyup", { e ->
        val event = e as KeyboardEvent
        val key = event.key
        if (key == "ArrowDown") {
            event.preventDefault()
            blocksGame.setDownHeld(false)
        }
    })

    var lastTimeMs = 0.0
    fun gameLoop(timestampMs: Double) {
        val deltaSec = if (lastTimeMs == 0.0) 0.0 else (timestampMs - lastTimeMs) / 1000.0
        lastTimeMs = timestampMs

        if (!generalGameOver) {
            snakeGame.update(deltaSec * speed.snakeMul)
            blocksGame.update(deltaSec * speed.blocksMul)
        } else {
            // On force l'arrêt des descentes accélérées.
            blocksGame.setDownHeld(false)
        }

        if (!generalGameOver && (snakeGame.gameOver || blocksGame.gameOver)) {
            generalGameOver = true
            snakeGame.forceGameOver()
            blocksGame.forceGameOver()
            gameOverTextEl.textContent = "GAME OVER"
            overlayEl.classList.remove("hidden")
        }

        val snakeScore = snakeGame.score
        val tetrisScore = blocksGame.score
        val total = snakeScore + tetrisScore
        scoreSnakeEl.textContent = snakeScore.toString()
        scoreTetrisEl.textContent = tetrisScore.toString()
        scoreTotalEl.textContent = total.toString()

        snakeCtx.clearRect(0.0, 0.0, snakeCanvas.width.toDouble(), snakeCanvas.height.toDouble())
        blocksCtx.clearRect(0.0, 0.0, blocksCanvas.width.toDouble(), blocksCanvas.height.toDouble())

        snakeGame.draw(snakeCtx)
        blocksGame.draw(blocksCtx)

        window.requestAnimationFrame(::gameLoop)
    }

    window.requestAnimationFrame(::gameLoop)
}

private fun requireCanvas(id: String): HTMLCanvasElement {
    return document.getElementById(id) as? HTMLCanvasElement
        ?: error("Canvas introuvable: #$id")
}

private fun requireSpan(id: String): HTMLSpanElement {
    return document.getElementById(id) as? HTMLSpanElement
        ?: error("Span introuvable: #$id")
}

private fun requireDiv(id: String): HTMLDivElement {
    return document.getElementById(id) as? HTMLDivElement
        ?: error("Div introuvable: #$id")
}

private fun requireButton(id: String): HTMLButtonElement {
    return document.getElementById(id) as? HTMLButtonElement
        ?: error("Bouton introuvable: #$id")
}