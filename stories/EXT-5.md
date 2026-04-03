# EXT-5 — Envoyer la date de la partie avec le replay

**Phase :** 1
**Dépendances :** EXT-4 (envoi des replays), BACK-5 (nouveau format du payload)
**Composant :** Extension Chrome

## Description

Le backend attend désormais un payload wrappé `{replay: ..., start: ...}` sur `POST /api/replays` (cf. BACK-5). L'extension envoie actuellement le replay JSON brut. Il faut adapter l'extension pour :

1. Capturer le timestamp `start` de chaque partie lors de la collecte des table IDs
2. Wrapper le payload envoyé au backend avec `replay` et `start`

Le champ `start` est un timestamp Unix (string) disponible dans chaque objet table retourné par l'API BGA `getGames`. L'extension y a déjà accès dans `collectTableIds` (elle utilise `table.end` pour le cutoff) mais ne le capture pas.

## Tâches

- [ ] Modifier `collectTableIds` (`service-worker.js`) : retourner des objets `{tableId, start}` au lieu de simples `table_id`. Ignorer les tables dont `start` est absent ou `null` (ne pas les collecter).
- [ ] Adapter `sendReplays` (`service-worker.js`) : recevoir les objets `{tableId, start}` et passer le `start` lors de l'envoi
- [ ] Modifier le `fetch` vers `POST /api/replays` : envoyer `{replay: replayJson, start: parseInt(table.start, 10)}` au lieu du replay brut (on réutilise `replayJson` déjà parsé pour le check de status)
- [ ] Adapter le code appelant pour gérer le nouveau format de retour de `collectTableIds`

## Détails techniques

### Avant (format actuel)

`collectTableIds` retourne un tableau de `table_id` (entiers) :
```javascript
tableIds.push(table.table_id);
```

Le POST envoie le replay brut :
```javascript
body: replayBody
```

### Après (format attendu)

`collectTableIds` retourne un tableau d'objets, en ignorant les tables sans `start` :
```javascript
if (table.start != null) {
  tables.push({ tableId: table.table_id, start: table.start });
}
```

Le POST envoie le payload wrappé (réutilise `replayJson` déjà parsé pour le status check) :
```javascript
body: JSON.stringify({ replay: replayJson, start: parseInt(table.start, 10) })
```

## Critères d'acceptation

- Le payload envoyé à `POST /api/replays` a le format `{replay: {...}, start: <integer>}`
- Le champ `start` correspond au timestamp Unix du début de la partie (issu de `table.start` de l'API BGA)
- Les replays sont toujours envoyés et traités correctement par le backend (statuts 201, 200, 400 inchangés)
- Le filtre par date (cutoff) continue de fonctionner
- Les tables sans champ `start` sont ignorées lors de la collecte (pas de fetch replay inutile)
