# TetraSnake

Jeu web asymétrique en **Kotlin 100%**, rendu en **HTML5 `<canvas>`** avec une boucle `requestAnimationFrame`.

## Règles rapides

### À gauche : Snake (clavier `Z Q S D`)
- Déplacement sur grille (zone plus large : plus de cases)
- Collision murs / morsure du corps = Game Over (virage interdit s’il ferait entrer la tête dans le corps)
- Chaque point a une **couleur** : elle correspond au **type de pièce** envoyée au jeu de droite (I, O, T, J, L, S, Z)

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
`src/main/kotlin/Main.kt` : bootstrap DOM + création des jeux
`src/main/kotlin/ControleurTetrasnake.kt` : boucle `requestAnimationFrame`, clavier, scores, overlay
`src/main/kotlin/JeuSerpent.kt` : serpent (grille, collision, nourriture colorée)
`src/main/kotlin/JeuBlocs.kt` : blocs (pièces, gravité, rotation, lignes, aperçu suivant)
`src/main/kotlin/CouleursPieces.kt` : palette commune serpent ↔ types de pièces
`src/jsMain/resources/index.html` : structure de la page  
`src/jsMain/resources/style.css` : styles (mise en page, thème, canvas plein écran)