# BACK-2 — Base de données et schéma

**Phase :** 1 (parallélisable)
**Dépendances :** aucune
**Composant :** Backend Clojure

## Description

Mettre en place la base de données SQLite et le schéma pour stocker les informations des parties 1v1. Écrire les fonctions d'accès de base avec next.jdbc.

## Décisions techniques

- **BDD :** SQLite pour le MVP (migration vers PostgreSQL prévue à terme)
- **Lib d'accès :** next.jdbc + org.xerial/sqlite-jdbc
- **Migrations :** Script SQL brut exécuté au démarrage
- **Gestion des doublons :** `ON CONFLICT DO NOTHING` — l'idempotence est gérée au niveau BACK-4 (200 vs 201), `insert-game!` doit permettre de distinguer "inséré" de "déjà existant" via le nombre de rows affected

## Schéma

```sql
CREATE TABLE IF NOT EXISTS games (
  table_id         TEXT PRIMARY KEY,   -- table_id BGA, unique
  player1_id       TEXT NOT NULL,      -- player_id BGA du joueur 1
  player1_hero     TEXT NOT NULL,
  player1_faction  TEXT NOT NULL,
  player1_deck_name TEXT NOT NULL,
  player1_deck_id  TEXT NOT NULL,
  player2_id       TEXT NOT NULL,      -- player_id BGA du joueur 2
  player2_hero     TEXT NOT NULL,
  player2_faction  TEXT NOT NULL,
  player2_deck_name TEXT NOT NULL,
  player2_deck_id  TEXT NOT NULL,
  winner_player_id TEXT NOT NULL,      -- player_id BGA du gagnant (= player1_id ou player2_id)
  created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Tâches

- [ ] Ajouter les dépendances next.jdbc + sqlite-jdbc au projet
- [ ] Créer le script SQL de migration (`resources/migrations/001-create-games.sql`)
- [ ] Configurer la connexion BDD (datasource SQLite, fichier `altered_history.db`)
- [ ] Exécuter le script de migration au démarrage de l'application
- [ ] Écrire `insert-game!` — insertion avec `ON CONFLICT DO NOTHING`, retourne si la ligne a été insérée ou non
- [ ] Écrire `game-exists?` — retourne `true`/`false` selon la présence du `table_id`
- [ ] Écrire les tests unitaires

## Critères d'acceptation

- La BDD est créée automatiquement au démarrage avec le schéma défini
- `insert-game!` insère une partie et retourne un indicateur "inserted" / "already_exists"
- `insert-game!` sur un `table_id` dupliqué ne crash pas (ON CONFLICT DO NOTHING)
- `game-exists?` retourne `true`/`false` selon la présence du `table_id`
- Les tests passent sur une BDD SQLite in-memory
