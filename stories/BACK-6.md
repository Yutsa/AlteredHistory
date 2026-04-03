# BACK-6 — Endpoint GET /api/games/:id (détail d'une partie)

**Phase :** 1
**Dépendances :** BACK-2 (base SQLite existante)
**Composant :** Backend

## Description

Créer un endpoint qui retourne le détail complet d'une partie à partir de son `table_id`. Cet endpoint sert la page détail côté frontend.

## Tâches

- [ ] Créer la route `GET /api/games/:id` dans `routes.clj`
- [ ] Implémenter le handler qui récupère la partie par `table_id`
- [ ] Retourner toutes les informations de la partie : `table_id`, `created_at`, les deux joueurs (id, héros, faction, deck_name), le gagnant
- [ ] Gérer le cas partie introuvable (404)
- [ ] Écrire les tests (partie existante, partie introuvable, format de réponse)

## Critères d'acceptation

- `GET /api/games/123` retourne le détail complet de la partie 123
- Une partie inexistante retourne 404 avec un message explicite
- La réponse JSON contient tous les champs de la table `games`
