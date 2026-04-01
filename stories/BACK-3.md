# BACK-3 — Parser de replay

**Phase :** 2 (séquentielle)
**Dépendances :** BACK-1, BACK-2
**Composant :** Backend Clojure

## Description

Écrire la fonction de parsing du JSON de replay BGA. C'est la pièce centrale du MVP : elle transforme le JSON brut de replay en données structurées pour la BDD.

## Données à extraire

| Donnée | Source dans le replay | Chemin |
|--------|----------------------|--------|
| table_id | Tout événement | `logs[].table_id` |
| Héros (nom) | `setupPlayer` | `args.card.properties.name` |
| Faction | `setupPlayer` | `args.card.properties.faction` |
| Player ID | `setupPlayer` | `args.player_id` |
| Nom du deck | `updateInitialPrecoDeckSelection` | `args.args._private.API.deckName` |
| ID du deck | `updateInitialPrecoDeckSelection` | `args.args._private.API.id` |
| Gagnant | `gameStateChange` (final) | `args.result[].score == "1"` ou `rank == 1` |

## Tâches

- [ ] Écrire la fonction de parsing du JSON de replay BGA :
  - Extraire les événements `setupPlayer` → héros + faction pour chaque joueur
  - Extraire les événements `updateInitialPrecoDeckSelection` → nom et ID du deck pour chaque joueur
  - Extraire le `gameStateChange` final avec `result` → déterminer le gagnant
  - Extraire le `table_id`
- [ ] Gérer les cas d'erreur : replay incomplet, événements manquants
- [ ] Écrire des tests unitaires avec des fixtures de replay réels

## Notes techniques

- Les événements `setupPlayer` sont sur le canal public `/table/t{table_id}` (un par joueur)
- Les événements `updateInitialPrecoDeckSelection` sont sur les canaux privés `/player/p{player_id}` (un par joueur) — en mode replay, les deux sont visibles
- Le gagnant est le joueur avec `score == "1"` dans le tableau `result` du dernier `gameStateChange`

## Critères d'acceptation

- Le parser extrait correctement héros, faction, deck name, deck id pour les deux joueurs
- Le parser identifie correctement le gagnant
- Les replays incomplets (événements manquants) retournent une erreur explicite
- Les tests passent sur au moins 2 fixtures de replay réels
