# BACK-2 — Base de données et schéma

**Phase :** 1 (parallélisable)
**Dépendances :** aucune
**Composant :** Backend Clojure

## Description

Mettre en place la base de données et le schéma pour stocker les informations des parties. Écrire les fonctions d'accès de base.

## Schéma

```sql
CREATE TABLE games (
  table_id         TEXT PRIMARY KEY,   -- table_id BGA, unique
  player1_hero     TEXT NOT NULL,
  player1_faction  TEXT NOT NULL,
  player1_deck_name TEXT,
  player1_deck_id  TEXT,
  player2_hero     TEXT NOT NULL,
  player2_faction  TEXT NOT NULL,
  player2_deck_name TEXT,
  player2_deck_id  TEXT,
  winner_player_id TEXT NOT NULL,       -- player_id BGA du gagnant
  created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Tâches

- [ ] Choisir la BDD (SQLite pour le MVP ou PostgreSQL)
- [ ] Créer le schéma (migration ou script SQL)
- [ ] Configurer la connexion BDD (next.jdbc ou similaire)
- [ ] Écrire les fonctions d'accès : `insert-game!`, `game-exists?`

## Critères d'acceptation

- La BDD est créée avec le schéma défini
- `insert-game!` insère une partie et retourne un résultat de succès
- `game-exists?` retourne `true`/`false` selon la présence du `table_id`
- L'insertion d'un `table_id` dupliqué est gérée proprement (pas de crash)
