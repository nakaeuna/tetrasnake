# TetraSnake

> Asymmetric dual web game combining Snake and falling blocks, written 100% in Kotlin/JS with HTML5 Canvas.

![Context](https://img.shields.io/badge/Context-Personal%20Project-6f42c1?style=flat-square)
![Kotlin/JS](https://img.shields.io/badge/Kotlin%2FJS-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![HTML5 Canvas](https://img.shields.io/badge/HTML5%20Canvas-E34F26?style=flat-square&logo=html5&logoColor=white)
[![Play Online](https://img.shields.io/badge/Play%20Online-GitHub%20Pages-2ea44f?style=flat-square&logo=github)](https://nakaeuna.github.io/tetrasnake/)

## Overview

**TetraSnake** is an asymmetric dual-board web game built entirely in **Kotlin/JS** and rendered on **HTML5 `<canvas>`** using a `requestAnimationFrame` game loop. Two classic games run side by side and depend on each other: eating colored food in Snake generates the corresponding falling tetrominoes on the right board.

👉 **[Play directly in your browser](https://nakaeuna.github.io/tetrasnake/)**

## Gameplay & Rules

- **Left board — Snake (`Z Q S D`)**
  - Grid-based movement across a wider playfield.
  - Hitting a wall or biting the snake's own body triggers a Game Over (instant 180° self-collision turns are prevented).
  - Each food item has a **color** mapped to a specific **tetromino type** (`I`, `O`, `T`, `J`, `L`, `S`, `Z`) sent to the right board.
- **Right board — Falling Blocks (Arrow keys)**
  - Rotating tetrominoes with gravity, collision detection, and line clearing.
  - The board stays idle whenever no piece is queued from the Snake board.
- **Shared Game Over**: if either board ends, both games stop simultaneously.

## Controls

| Board / Action | Keys |
|---|---|
| Snake movement | `Z` (up), `Q` (left), `S` (down), `D` (right) |
| Move block | `←` / `→` |
| Rotate block | `↑` |
| Soft drop | `↓` |
| Restart game | `R` |

## Tech Stack

- **Kotlin/JS** (Gradle)
- **HTML5 `<canvas>`** & DOM API (`requestAnimationFrame`)
- **HTML5 / CSS3**

## Project Structure

```text
├── build.gradle.kts
├── .github/workflows/deploy-pages.yml  # Automatic GitHub Pages deployment
└── src/
    ├── jsMain/resources/
    │   ├── index.html                  # Page layout & canvas elements
    │   └── style.css                   # Theme and responsive fullscreen canvas styling
    └── main/kotlin/
        ├── Main.kt                     # DOM bootstrap & game initialization
        ├── ControleurTetrasnake.kt     # Game loop, keyboard handling, scores & overlays
        ├── JeuSerpent.kt               # Snake grid, collisions & colored food
        ├── JeuBlocs.kt                 # Falling pieces, gravity, rotation, line clearing & preview
        └── CouleursPieces.kt           # Shared color palette linking food to piece types
```

## Getting Started

```bash
./gradlew jsBrowserDevelopmentRun
```

Then open `http://localhost:8080/` in your browser (opens automatically in most environments).