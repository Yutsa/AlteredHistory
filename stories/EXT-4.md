# EXT-4 — Collecte et envoi des replays

**Phase :** 2 (séquentielle)
**Dépendances :** EXT-1, EXT-2, EXT-3
**Composant :** Extension Chrome

## Description

Implémenter le flux complet de collecte des parties : pagination de `getGames` pour collecter les `table_id`, puis récupération et envoi de chaque replay au backend.

## Tâches

### Phase 1 — UI et déclenchement

- [ ] Ajouter dans le popup (visible uniquement si connecté) :
  - Un champ `datetime-local` pour la date butoir
  - Un bouton "Importer"
- [ ] Pré-remplir le champ date avec la date du dernier export réussi (stockée dans `chrome.storage.local` sous `lastExportDate`). Si absente, pré-remplir à J-7.
- [ ] Le bouton envoie un message au service worker pour lancer la collecte (désactivé pendant l'import)

### Phase 2 — Collecte des table_id

- [ ] Paginer `getGames.html` (`game_id=1909` hardcodé) pour collecter les `table_id`
- [ ] Rate limiting : délai aléatoire entre 0.6 et 1s entre chaque requête
- [ ] Condition d'arrêt : tableau `data.tables` vide OU date de la partie (`end`) antérieure à la date butoir saisie
- [ ] Retry en cas d'erreur (hors 403) : 3 tentatives avec délai exponentiel (1s, 2s, 4s), puis skip de la page
- [ ] En cas de 403 : arrêter immédiatement l'import, vider le storage local (session invalide), afficher "Session expirée, reconnectez-vous sur BGA"

### Phase 3 — Envoi des replays

- [ ] Pour chaque `table_id`, appeler `logs.html` pour récupérer le replay JSON complet
- [ ] Envoyer chaque replay **tel quel, sans transformation** au backend via `POST /api/replays`
- [ ] Rate limiting : délai aléatoire entre 0.6 et 1s entre chaque appel à BGA
- [ ] Retry en cas d'erreur (hors 403) : 3 tentatives avec délai exponentiel (1s, 2s, 4s), puis skip de ce replay et comptabiliser l'échec
- [ ] En cas de 403 : même comportement que Phase 2 (arrêt immédiat, message session expirée)

### Architecture — Service worker et popup

- [ ] Toute la logique de collecte (pagination, fetch replays, envoi backend) tourne dans le **service worker**, indépendamment du popup
- [ ] Le popup envoie un message au service worker pour démarrer l'import et lit l'état de progression depuis `chrome.storage.local`
- [ ] Le service worker met à jour `chrome.storage.local` avec l'état courant de l'import (progression, compteurs, statut) pour que le popup puisse l'afficher à tout moment (y compris après fermeture/réouverture)

### Phase 4 — Progression et bilan

- [ ] Pendant l'import : afficher la progression dans le popup (X/Y parties envoyées + progress bar avec pourcentage). Chaque réponse du backend fait avancer le compteur.
- [ ] À la fin de l'import : afficher un message bilan avec 4 catégories :
  - **Importées** : réponse 201 (créé)
  - **Déjà existantes** : réponse 200 `already_exists`
  - **Skippées** : réponse 200 `skipped` (precon/égalité)
  - **Échouées** : erreurs après 3 retries (BGA inaccessible ou backend 400)
- [ ] Stocker `lastExportDate` dans `chrome.storage.local` (date/heure du lancement de l'import)

## Headers requis pour les appels BGA

| Header | Valeur |
|--------|--------|
| `X-Request-Token` | Token récupéré via EXT-2 |
| `X-Requested-With` | `XMLHttpRequest` |
| `Referer` | URL BGA cohérente |

## Critères d'acceptation

- Le bouton d'import et le champ date ne sont visibles que si l'utilisateur est connecté (EXT-3)
- Le champ date est pré-rempli avec la date du dernier export, ou J-7 par défaut
- Les `table_id` sont collectés page par page avec respect du rate limiting (0.6-1s aléatoire)
- La pagination s'arrête correctement (tableau vide ou date butoir atteinte)
- Les erreurs BGA/backend (hors 403) sont retryées 3 fois (délai exponentiel 1/2/4s) puis skippées
- Un 403 BGA stoppe immédiatement l'import, vide le storage local et affiche "Session expirée, reconnectez-vous sur BGA"
- Chaque replay est envoyé tel quel au backend sans transformation
- L'import tourne dans le service worker et continue même si le popup est fermé
- Le popup affiche la progression en temps réel (X/Y + progress bar) en lisant l'état depuis `chrome.storage.local`
- Un bilan est affiché à la fin avec 4 catégories : importées / déjà existantes / skippées / échouées
- La date du dernier export est persistée pour pré-remplir le champ au prochain usage
