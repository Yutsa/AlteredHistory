# EXT-4 — Collecte et envoi des replays

**Phase :** 2 (séquentielle)
**Dépendances :** EXT-1, EXT-2, EXT-3
**Composant :** Extension Chrome

## Description

Implémenter le flux complet de collecte des parties : pagination de `getGames` pour collecter les `table_id`, puis récupération et envoi de chaque replay au backend.

## Tâches

### Phase 1 — Collecte des table_id

- [ ] Paginer `getGames.html` pour collecter les `table_id` (avec rate limiting 1.5s entre chaque requête)
- [ ] Gérer la condition d'arrêt (tableau `data.tables` vide ou date limite atteinte)

### Phase 2 — Envoi des replays

- [ ] Pour chaque `table_id`, appeler `logs.html` pour récupérer le replay JSON complet
- [ ] Envoyer chaque replay **tel quel, sans transformation** au backend via `POST /api/replays`
- [ ] Respecter le rate limiting (1.5s entre chaque appel à BGA)

### UI

- [ ] Afficher la progression dans le popup (X/Y parties envoyées)

## Headers requis pour les appels BGA

| Header | Valeur |
|--------|--------|
| `X-Request-Token` | Token récupéré via EXT-2 |
| `X-Requested-With` | `XMLHttpRequest` |
| `Referer` | URL BGA cohérente |

## Critères d'acceptation

- Les `table_id` sont collectés page par page avec respect du rate limiting
- La pagination s'arrête correctement (tableau vide ou date limite)
- Chaque replay est envoyé tel quel au backend sans transformation
- L'utilisateur voit la progression dans le popup
