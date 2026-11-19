# TetraSnake

> Asymmetric dual web game combining Snake and falling blocks, written in Kotlin/JS with HTML5 Canvas.

![Context](https://img.shields.io/badge/Context-Personal%20Project-6f42c1?style=flat-square)
![Kotlin/JS](https://img.shields.io/badge/Kotlin%2FJS-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![HTML5 Canvas](https://img.shields.io/badge/HTML5%20Canvas-E34F26?style=flat-square&logo=html5&logoColor=white)
[![Play Online](https://img.shields.io/badge/Play%20Online-GitHub%20Pages-7F52FF?style=flat-square&logo=github&logoColor=white)](https://nakaeuna.github.io/tetrasnake/)

## Overview

**TetraSnake** is an asymmetric dual-board web game built in **Kotlin/JS** and rendered on **HTML5 `<canvas>`** with a `requestAnimationFrame` game loop. Two classic games run side by side and depend on each other. Eating colored food in Snake sends the matching falling block to the right board.

**[Play directly in your browser](https://nakaeuna.github.io/tetrasnake/)**

## Gameplay and Rules

- **Left board (Snake, `Z Q S D`):**
  - Grid movement across a wide playfield.
  - Hitting a wall or biting the body ends the game. Instant opposite turns are blocked.
  - Each food color matches one block shape (`I`, `O`, `T`, `J`, `L`, `S`, `Z`) sent to the right board.
- **Right board (Falling Blocks, arrow keys):**
  - Rotating pieces with gravity, collision checks, and line clearing.
  - The board waits when no piece is available from the Snake board.
- **Shared Game Over:** If either game ends, both games stop at once.

## Controls

| Action | Keys |
|---|---|
| Snake movement | `Z` (up), `Q` (left), `S` (down), `D` (right) |
| Move block | `←` / `→` |
| Rotate block | `↑` |
| Soft drop | `↓` |
| Restart game | `R` |

## Technology Stack

- **Kotlin/JS** and Gradle
- **HTML5 `<canvas>`** and DOM API (`requestAnimationFrame`)
- **HTML5** and **CSS3**

## Project Structure

```text
├── build.gradle.kts
├── .github/workflows/deploy-pages.yml  # Automatic GitHub Pages deployment
└── src/
    ├── jsMain/resources/
    │   ├── index.html                  # Page layout and canvas elements
    │   └── style.css                   # Theme and fullscreen canvas styling
    └── main/kotlin/
        ├── Main.kt                     # DOM bootstrap and game initialization
        ├── ControleurTetrasnake.kt     # Game loop, keyboard handling, scores, and overlays
        ├── JeuSerpent.kt               # Snake grid, collisions, and colored food
        ├── JeuBlocs.kt                 # Falling pieces, gravity, rotation, line clearing, and preview
        └── CouleursPieces.kt           # Shared color palette linking food to piece types
```

## Getting Started

```bash
./gradlew jsBrowserDevelopmentRun
```

Then open `http://localhost:8080/` in your browser.

## License

All Rights Reserved. See [`LICENSE`](LICENSE) for details. No commercial use, modification, or redistribution is allowed without written permission.