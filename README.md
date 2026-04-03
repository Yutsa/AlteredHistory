# Altered History

Suivi d'historique de parties [Altered](https://www.altered.gg/) jouées sur [Board Game Arena](https://boardgamearena.com/).

Une **extension Chrome** collecte les données de parties depuis BGA et les envoie à un **backend Clojure** qui les stocke en SQLite.

## Lancer le backend

```bash
cd backend && clj -M -m altered-history.core
```

## Lancer les tests

```bash
cd backend && clj -M:test
```
