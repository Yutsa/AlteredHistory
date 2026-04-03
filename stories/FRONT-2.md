# FRONT-2 — Page de recherche par pseudo

**Phase :** 2
**Dépendances :** FRONT-1 (scaffolding)
**Composant :** Frontend

## Description

Créer la page d'accueil avec un champ de recherche permettant d'entrer un pseudo BGA. La soumission redirige vers la page d'historique du joueur.

## Tâches

- [ ] Créer le composant de page d'accueil avec titre et champ de recherche
- [ ] Gérer l'état du champ (saisie du pseudo)
- [ ] À la soumission (Enter ou bouton), naviguer vers la route `/players/:name/games`
- [ ] Gérer le cas champ vide (pas de navigation)
- [ ] Appliquer un style minimal (centré, lisible)

## Critères d'acceptation

- La page affiche un champ de recherche avec un placeholder explicite ("Entrer un pseudo BGA")
- La soumission avec un pseudo non-vide navigue vers `/players/:pseudo/games`
- La soumission avec un champ vide ne fait rien
- Le champ est focusé au chargement de la page
