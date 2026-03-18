# TetraSnake

Jeu web asymétrique en **Kotlin 100%**, rendu en **HTML5 `<canvas>`** avec une boucle `requestAnimationFrame`.

## Règles rapides

### À gauche : Snake (clavier `Z Q S D`)
- Déplacement sur grille
- Collision murs / collision avec soi = Game Over
- Quand le snake mange un point, il déclenche une pièce pour le jeu de droite

### À droite : Blocs qui tombent
- Pièces rotatives avec gravité
- Collision et effacement des lignes
- Le jeu reste vide si aucune pièce n’est disponible

### Game over général : 
- si l’un des deux jeux se termine, les deux s’arrêtent

## Contrôles

### Snake :
  - `Z` : haut
  - `Q` : gauche
  - `S` : bas
  - `D` : droite

### Blocs :
  - `←` / `→` : déplacer
  - `↑` : rotation
  - `↓` : descente accélérée (soft drop)

### Relancer :
  - `R` : Relancer une partie

## Lancer le projet

```bash
./gradlew jsBrowserDevelopmentRun
```

Ouvre ensuite l’URL affichée `http://localhost:8080/`. Elle s'ouvre automatiquement dans le navigateur la plupart du temps.

## Architecture
`src/main/kotlin/Main.kt` : orchestration, boucle de rendu, clavier, overlay game over, scores
`src/main/kotlin/SnakeGame.kt` : logique snake (grille, collision, nourriture)
`src/main/kotlin/BlocksGame.kt` : logique blocs (pièces, gravité, rotation, lignes, encart NEXT)
`src/jsMain/resources/index.html` : UI (scores, overlay, style arcade)