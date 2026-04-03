# BACK-5 — Endpoint GET /api/players/:name/games (historique paginé)

**Phase :** 1
**Dépendances :** BACK-2 (base SQLite existante), BACK-4 (données en base)
**Composant :** Backend

## Description

Créer un endpoint qui retourne l'historique des parties d'un joueur donné, identifié par son pseudo BGA. La réponse est paginée pour gérer les gros volumes.

La recherche se fait par pseudo exact, case-insensitive, sur les champs `player1_name` / `player2_name` de la table `games`.

## Pré-requis (modifications du schéma et du parser existants)

Ces modifications sont nécessaires avant d'implémenter l'endpoint lui-même :

- [ ] Ajouter les colonnes `player1_name TEXT NOT NULL` et `player2_name TEXT NOT NULL` dans la table `games` (nouvelle migration)
- [ ] Ajouter la colonne `played_at INTEGER NOT NULL` dans la table `games` (timestamp Unix du début de partie)
- [ ] Modifier `replay_parser.clj` : extraire `player_name` depuis les événements `setupPlayer` (`args.player_name`)
- [ ] Modifier `POST /api/replays` : accepter un champ `start` (timestamp Unix) dans le payload, le stocker en `played_at`
- [ ] Mettre à jour `db/insert-game!` pour inclure `player1_name`, `player2_name` et `played_at`
- [ ] Mettre à jour les tests existants (replay parser, routes, db) pour refléter les nouveaux champs

## Tâches endpoint

- [ ] Créer la route `GET /api/players/:name/games` dans `routes.clj`
- [ ] Implémenter la query SQL paginée (paramètres `page` et `page_size`, défaut page=1, page_size=20)
- [ ] Recherche case-insensitive sur `player1_name` et `player2_name` (exact match, pas de LIKE partiel)
- [ ] Tri par `played_at` décroissant (parties les plus récentes en premier)
- [ ] Retourner pour chaque partie : `table_id`, `played_at`, adversaire (`id` + `name`), faction et héros de chaque joueur, nom du deck de chaque joueur, `won` (booléen, du point de vue du joueur recherché)
- [ ] Retourner les métadonnées de pagination : `page`, `page_size`, `total_count`, `total_pages`
- [ ] Gérer le cas joueur introuvable (404 avec message explicite)
- [ ] Écrire les tests (pagination, joueur inconnu, parties en tant que player1 et player2, case-insensitivity)

## Format de réponse

```json
{
  "page": 1,
  "page_size": 20,
  "total_count": 42,
  "total_pages": 3,
  "games": [
    {
      "table_id": 829783480,
      "played_at": 1774773311,
      "opponent": {
        "id": "95656346",
        "name": "Songlu"
      },
      "player": {
        "hero": "Akesha & Taru",
        "faction": "YZ",
        "deck_name": "Akesha Burst 4"
      },
      "opponent_details": {
        "hero": "Della & Bolt",
        "faction": "AX",
        "deck_name": "DellaS5"
      },
      "won": true
    }
  ]
}
```

## Critères d'acceptation

- `GET /api/players/Yutsa/games` retourne les parties de Yutsa avec pagination
- `GET /api/players/yutsa/games` retourne les mêmes résultats (case-insensitive)
- `GET /api/players/Yutsa/games?page=2&page_size=10` retourne la page 2 avec 10 résultats max
- Le champ `won` indique victoire/défaite du point de vue du joueur recherché
- Un joueur inconnu retourne 404 avec un message explicite
- Les parties où le joueur est player1 ET player2 sont incluses
- Les parties sont triées par date décroissante
