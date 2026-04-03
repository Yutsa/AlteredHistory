# FRONT-1 — Scaffolding ClojureScript + Replicant

**Phase :** 2
**Dépendances :** aucune
**Composant :** Frontend

## Description

Mettre en place le projet frontend ClojureScript avec Replicant comme bibliothèque de rendu. Le projet doit compiler, se lancer en mode dev avec hot-reload, et afficher une page placeholder.

Le frontend est placé dans le dossier `frontend/` à la racine du repo.

## Tâches

- [ ] Créer le dossier `frontend/` avec la structure de projet ClojureScript (deps.edn, index.html, src/)
- [ ] Configurer shadow-cljs comme outil de build
- [ ] Ajouter Replicant comme dépendance
- [ ] Créer le namespace principal `altered-history.core` avec un rendu Replicant minimal (titre "Altered History")
- [ ] Configurer le hot-reload en mode dev
- [ ] Vérifier que `npx shadow-cljs watch app` compile et affiche la page dans le navigateur
- [ ] Ajouter un routing client-side minimal (hash-based ou history API)

## Critères d'acceptation

- `npx shadow-cljs watch app` compile sans erreur
- La page s'affiche dans le navigateur avec le titre "Altered History"
- Le hot-reload fonctionne (un changement dans le code se reflète dans le navigateur)
- La structure du projet est propre et suit les conventions ClojureScript
