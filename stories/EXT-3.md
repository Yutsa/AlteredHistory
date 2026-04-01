# EXT-3 — Détection de la session BGA et récupération du player ID

**Phase :** 1 (parallélisable)
**Dépendances :** aucune
**Composant :** Extension Chrome

## Description

Vérifier que l'utilisateur est connecté à BGA et récupérer son player ID. Le popup doit afficher l'état de connexion.

## Tâches

- [ ] Vérifier la présence d'un cookie de session BGA valide
- [ ] Récupérer l'ID du joueur connecté (depuis `bgaConfig` ou un endpoint BGA)
- [ ] Afficher dans le popup l'état de connexion (connecté / non connecté)

## Critères d'acceptation

- Le popup affiche "Connecté" avec le pseudo du joueur quand la session BGA est active
- Le popup affiche "Non connecté" avec un message invitant à se connecter sur BGA sinon
- Le player ID est récupéré et disponible pour les appels API (paramètre `player` de `getGames`)
