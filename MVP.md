# MVP 2 — Frontend Historique des parties

## Objectif

Permettre à n'importe qui de consulter l'historique des parties d'un joueur BGA en entrant son pseudo. L'historique est paginé et affiche les informations clés de chaque partie.

## Stack

- **Frontend** : ClojureScript + Replicant (rendu hiccup, zéro dépendance React)
- **Backend** : Clojure (existant) — nouveaux endpoints API

## Fonctionnalités

### Recherche par pseudo

- Un champ de recherche permet d'entrer un pseudo BGA
- La recherche renvoie l'historique des parties de ce joueur

### Liste des parties (paginée)

Pour chaque partie, afficher :
- Date de la partie
- Adversaire
- Faction et héros de chaque joueur
- Nom du deck de chaque joueur
- Résultat (victoire / défaite)

La liste est paginée pour gérer les gros volumes.

### Page détail d'une partie (optionnel)

- Vue détaillée reprenant toutes les infos d'une partie
- Lien vers le replay BGA si disponible

---

## Phases

```
Phase 1 — API Backend (séquentielle) :
  ├── BACK-5  Endpoint GET /api/players/:name/games (paginé)
  └── BACK-6  Endpoint GET /api/games/:id (détail d'une partie)

Phase 2 — Frontend (séquentielle) :
  ├── FRONT-1  Scaffolding projet ClojureScript + Replicant
  ├── FRONT-2  Page recherche par pseudo
  └── FRONT-3  Page historique avec pagination

Phase 3 — Intégration :
  └── INT-3    Connexion front/back, cas limites
```

---

## Tâches

### Légende

- `[ ]` À faire
- `[~]` En cours
- `[x]` Terminé

### Backend

| Story | Description | Status |
|-------|-------------|--------|
| [BACK-5](stories/BACK-5.md) | Endpoint GET /api/players/:name/games (historique paginé) | [ ] |
| [BACK-6](stories/BACK-6.md) | Endpoint GET /api/games/:id (détail partie) | [ ] |

### Frontend

| Story | Description | Status |
|-------|-------------|--------|
| [FRONT-1](stories/FRONT-1.md) | Scaffolding ClojureScript + Replicant | [ ] |
| [FRONT-2](stories/FRONT-2.md) | Page de recherche par pseudo | [ ] |
| [FRONT-3](stories/FRONT-3.md) | Page historique paginée | [ ] |

### Intégration

| Story | Description | Status |
|-------|-------------|--------|
| [INT-3](stories/INT-3.md) | Intégration front/back + cas limites | [ ] |

---

## Hors scope

- Authentification utilisateur
- Statistiques avancées (winrate, matchups, etc.)
- Déploiement en production
- Filtres ou tri sur l'historique
- Page détail d'une partie (optionnel, à réévaluer)
