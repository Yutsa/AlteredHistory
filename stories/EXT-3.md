# EXT-3 — Détection de la session BGA et récupération du player ID

**Phase :** 1 (parallélisable)
**Dépendances :** aucune
**Composant :** Extension Chrome

## Description

Vérifier que l'utilisateur est connecté à BGA et récupérer son player ID. Le popup doit afficher l'état de connexion.

### Prérequis utilisateur

L'utilisateur doit être sur sa page d'historique BGA (`https://boardgamearena.com/player?id=...&section=lastresults`) et être connecté.

### Sources de données

- **Player ID** : extrait du query param `id` de l'URL de l'onglet actif
- **Pseudo** : extrait du DOM via `<span id="real_player_name">`
- **Session active** : vérifiée par la présence du cookie `PHPSESSID` via `chrome.cookies.get()`

### Flux

L'extraction se fait **à la demande** : quand l'utilisateur ouvre le popup, celui-ci vérifie l'onglet actif et en extrait les informations si les conditions sont réunies. Pas d'extraction passive au chargement de page (contrairement à EXT-2).

1. Le popup s'ouvre
2. Il vérifie que l'onglet actif est sur `boardgamearena.com/player?id=...&section=lastresults`
3. Il vérifie la présence du cookie `PHPSESSID` via `chrome.cookies.get()`
4. Il injecte un script dans l'onglet actif pour lire `<span id="real_player_name">`
5. Il extrait le player ID depuis le query param `id` de l'URL
6. Il stocke `{ playerId, playerName }` dans `chrome.storage.local`

### Stockage

`chrome.storage.local` : `{ playerId: "91926087", playerName: "Yutsa" }` (aux côtés du `requestToken` déjà stocké par EXT-2).

### Session expirée

La détection de session expirée n'est pas gérée par cette story. Elle sera détectée à l'import (EXT-4) : en cas d'erreur API, le storage local sera vidé, ce qui repassera l'état en "non connecté".

## Tâches

- [ ] Ajouter la permission `cookies` dans `manifest.json`
- [ ] Au clic sur le popup, vérifier que l'URL de l'onglet actif matche `boardgamearena.com/player?id=...&section=lastresults`
- [ ] Vérifier la présence du cookie `PHPSESSID` sur le domaine `boardgamearena.com` via `chrome.cookies.get()`
- [ ] Injecter un script dans l'onglet actif pour extraire le pseudo depuis `<span id="real_player_name">`
- [ ] Extraire le player ID depuis le query param `id` de l'URL
- [ ] Stocker `playerId` et `playerName` dans `chrome.storage.local`
- [ ] Afficher dans le popup l'état de connexion (connecté avec pseudo / message invitant à aller sur la page d'historique)

## Critères d'acceptation

- Le popup affiche "Connecté" avec le pseudo du joueur quand toutes les conditions sont réunies (bonne page, cookie présent, pseudo trouvé)
- Le popup affiche un message invitant l'utilisateur à se rendre sur sa page d'historique BGA en étant connecté sinon
- Le player ID et le pseudo sont persistés dans `chrome.storage.local` et disponibles pour les appels API (paramètre `player` de `getGames`)
