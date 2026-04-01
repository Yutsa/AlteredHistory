# EXT-2 — Récupération du X-Request-Token

**Phase :** 1 (parallélisable)
**Dépendances :** aucune
**Composant :** Extension Chrome

## Description

Implémenter le mécanisme d'extraction du token CSRF (`X-Request-Token`) depuis la page BGA. Ce token est requis pour tous les appels à l'API BGA.

## Contexte technique

Le token est accessible via `bgaConfig.requestToken`, une variable globale JavaScript disponible sur toutes les pages BGA. Les content scripts s'exécutent dans un monde isolé, il faut donc utiliser `chrome.scripting.executeScript` avec `world: 'MAIN'` pour accéder au contexte de la page.

## Tâches

- [ ] Injecter un script dans le contexte de la page BGA (via `chrome.scripting.executeScript` avec `world: 'MAIN'`)
- [ ] Lire `bgaConfig.requestToken` et le communiquer au service worker
- [ ] Gérer le cas où l'utilisateur n'est pas connecté (token absent)

## Critères d'acceptation

- Le token est récupéré quand l'utilisateur est connecté à BGA
- Un message d'erreur clair est remonté si le token est absent (utilisateur non connecté)
- Le token est disponible dans le service worker pour les appels API ultérieurs
