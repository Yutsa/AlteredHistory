# EXT-2 — Récupération du X-Request-Token

**Phase :** 1 (parallélisable)
**Dépendances :** aucune
**Composant :** Extension Chrome

## Description

Implémenter le mécanisme d'extraction du token CSRF (`X-Request-Token`) depuis la page BGA. Ce token est requis pour tous les appels à l'API BGA.

## Contexte technique

Le token est accessible via `bgaConfig.requestToken`, une variable globale JavaScript disponible sur toutes les pages BGA. Les content scripts s'exécutent dans un monde isolé, il faut donc utiliser `chrome.scripting.executeScript` avec `world: 'MAIN'` pour accéder au contexte de la page.

### Communication MAIN world → Service Worker

Le script injecté dans le MAIN world n'a pas accès aux APIs Chrome. Le flux de communication est :
1. Le script MAIN lit `bgaConfig.requestToken` et poste un `window.postMessage`
2. Un content script (monde isolé) écoute le message et le relaye via `chrome.runtime.sendMessage` au service worker

### Déclenchement

L'extraction est déclenchée automatiquement au chargement de chaque page BGA (via content script injecté sur `*://boardgamearena.com/*`).

### Stockage

Le token est persisté dans `chrome.storage.local` pour survivre aux redémarrages du service worker (cycle de vie MV3).

## Tâches

- [ ] Ajouter les permissions `scripting` et `activeTab` dans `manifest.json`
- [ ] Déclarer un content script sur `*://boardgamearena.com/*`
- [ ] Injecter un script dans le MAIN world (via `chrome.scripting.executeScript` avec `world: 'MAIN'`) qui lit `bgaConfig.requestToken` et le poste via `window.postMessage`
- [ ] Dans le content script (monde isolé), écouter le `window.postMessage` et relayer au service worker via `chrome.runtime.sendMessage`
- [ ] Dans le service worker, recevoir le token et le persister dans `chrome.storage.local`
- [ ] Logger dans la console du service worker : un log de confirmation quand le token est reçu, un warning s'il est absent

## Critères d'acceptation

- Le token est récupéré automatiquement au chargement de chaque page BGA quand l'utilisateur est connecté
- Si le token est absent, un warning est loggé dans la console du service worker (pas d'affichage UI — l'état de connexion est géré par EXT-3)
- Le token est persisté dans `chrome.storage.local` et disponible pour les appels API ultérieurs
