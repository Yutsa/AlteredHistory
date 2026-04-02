# MVP — AlteredHistory

## Objectif

Avoir un pipeline fonctionnel de bout en bout : l'extension Chrome récupère les replays BGA et les envoie au backend Clojure qui parse et stocke les informations essentielles de chaque partie.

## Données stockées par partie

- `table_id` (unique en BDD)
- Pour chaque joueur (x2) :
  - Héros (nom)
  - Faction
  - Nom du deck
  - ID du deck (API Altered)
- Gagnant de la partie

---

## Tâches

### Légende

- `[ ]` À faire
- `[~]` En cours
- `[x]` Terminé

### Parallélisation

```
Phase 1 (parallélisable) :
  ├── EXT-1, EXT-2, EXT-3    (Extension Chrome)
  └── BACK-1, BACK-2          (Backend Clojure)

Phase 2 (séquentielle, dépend de Phase 1) :
  ├── EXT-4   (dépend de EXT-1/2/3)
  ├── BACK-3  (dépend de BACK-1/2)
  └── BACK-4  (dépend de BACK-3)

Phase 3 (intégration, dépend de Phase 2) :
  └── INT-1   (dépend de EXT-4 + BACK-4)

Phase 4 (tests E2E) :
  └── INT-2   (dépend de INT-1)
```

---

### Extension Chrome

| Story | Description | Status |
|-------|-------------|--------|
| [EXT-1](stories/EXT-1.md) | Scaffolding de l'extension Manifest V3 | [x] |
| [EXT-2](stories/EXT-2.md) | Récupération du X-Request-Token | [x] |
| [EXT-3](stories/EXT-3.md) | Détection de la session BGA et récupération du player ID | [ ] |
| [EXT-4](stories/EXT-4.md) | Collecte et envoi des replays | [ ] |

### Backend Clojure

| Story | Description | Status |
|-------|-------------|--------|
| [BACK-1](stories/BACK-1.md) | Scaffolding du projet Clojure | [ ] |
| [BACK-2](stories/BACK-2.md) | Base de données et schéma | [ ] |
| [BACK-3](stories/BACK-3.md) | Parser de replay | [ ] |
| [BACK-4](stories/BACK-4.md) | Endpoint POST /api/replays | [ ] |

### Intégration

| Story | Description | Status |
|-------|-------------|--------|
| [INT-1](stories/INT-1.md) | Connecter l'extension au backend | [ ] |
| [INT-2](stories/INT-2.md) | Test end-to-end manuel | [ ] |

---

## Hors scope MVP

- Application web front-end (consultation)
- Authentification utilisateur côté backend
- Déploiement en production (le MVP tourne en local)
- Historique des cartes jouées pendant la partie
- Stockage des ELO / scores arena
