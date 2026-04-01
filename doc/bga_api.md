# API Board Game Arena — Historique de parties Altered

> L'API BGA n'est pas publique. Cette documentation est basée sur du reverse engineering et peut changer sans préavis.

## Flux d'import depuis l'extension

L'extension Chrome effectue l'import en deux phases. C'est le **backend** qui traite les données, l'extension ne fait que les relayer.

### Phase 1 — Collecte des `table_id`

L'extension pagine l'endpoint `getGames` pour récupérer la liste des parties. Elle collecte les `table_id` de chaque partie et s'arrête quand :
- `data.tables` est vide (plus de données), ou
- toutes les parties de la page ont un `start` antérieur à la date limite choisie par l'utilisateur.

**Aucune communication avec le backend** pendant cette phase.

### Phase 2 — Envoi des replays au backend

Pour chaque `table_id` collecté, l'extension appelle l'endpoint `logs.html` pour récupérer le JSON de replay complet, puis l'envoie **tel quel, sans transformation** au backend.

C'est le backend qui est responsable de parser chaque JSON de replay pour en extraire les informations utiles (héros, factions, decks, résultat, etc.) et construire l'historique des parties.

---

## Endpoint : liste des parties

```
GET https://boardgamearena.com/gamestats/gamestats/getGames.html
```

### Query params

| Paramètre          | Exemple          | Description                                                        |
|---------------------|------------------|--------------------------------------------------------------------|
| `player`           | `91926087`       | ID BGA du joueur dont on veut l'historique                         |
| `opponent_id`      | `0`              | Filtre par adversaire (`0` = tous)                                 |
| `game_id`          | `1909`           | ID du jeu sur BGA. **1909 = Altered**                              |
| `finished`         | `0`              | Filtre sur les parties terminées                                   |
| `page`             | `1`, `2`, ...    | Pagination (par défaut page 1)                                     |
| `updateStats`      | `1` ou `0`       | `1` sur le premier appel, `0` sur les suivants                     |
| `dojo.preventCache`| `1775031906924`  | Timestamp anti-cache (valeur arbitraire, typiquement `Date.now()`) |

### Exemple de requêtes

Première page (avec mise à jour des stats) :
```
https://boardgamearena.com/gamestats/gamestats/getGames.html?player=91926087&opponent_id=0&game_id=1909&finished=0&updateStats=1&dojo.preventCache=1775031906924
```

Pages suivantes (sans mise à jour des stats) :
```
https://boardgamearena.com/gamestats/gamestats/getGames.html?player=91926087&opponent_id=0&game_id=1909&finished=0&page=2&updateStats=0&dojo.preventCache=1775032023209
```

## Structure de la réponse

Le JSON retourné contient les données dans `data.tables`, un tableau d'objets représentant chaque partie.

### Chemin d'accès

```
response.data.tables[]
```

### Structure d'un objet partie

```json
{
    "table_id": "828463287",
    "game_name": "altered",
    "game_id": "1909",
    "ranking_disabled": "0",
    "start": "1774773311",
    "end": "1774774778",
    "concede": "1",
    "unranked": "0",
    "normalend": "1",
    "players": "97922800,91926087",
    "player_names": "Songlu,Yutsa",
    "scores": "1,0",
    "ranks": "1,2",
    "elo_win": "-2",
    "elo_penalty": "",
    "elo_after": "1472",
    "arena_win": "-0.0007",
    "arena_after": "501.1457"
}
```

### Description des champs

| Champ              | Type     | Description                                                                 |
|---------------------|----------|-----------------------------------------------------------------------------|
| `table_id`         | string   | Identifiant unique de la table/partie. Nécessaire pour récupérer le replay. |
| `game_name`        | string   | Nom du jeu (`"altered"`)                                                    |
| `game_id`          | string   | ID du jeu sur BGA (`"1909"` pour Altered)                                   |
| `ranking_disabled` | string   | `"1"` si le classement est désactivé pour cette partie                      |
| `start`            | string   | Timestamp Unix du début de la partie                                        |
| `end`              | string   | Timestamp Unix de la fin de la partie                                       |
| `concede`          | string   | `"1"` si la partie s'est terminée par concession                            |
| `unranked`         | string   | `"1"` si la partie est non classée                                          |
| `normalend`        | string   | `"1"` si la partie s'est terminée normalement                               |
| `players`          | string   | IDs BGA des joueurs, séparés par des virgules                               |
| `player_names`     | string   | Noms des joueurs, séparés par des virgules (même ordre que `players`)       |
| `scores`           | string   | Scores des joueurs, séparés par des virgules (même ordre que `players`)     |
| `ranks`            | string   | Classements des joueurs (`1` = premier, `2` = second, etc.)                 |
| `elo_win`          | string   | Variation d'ELO pour le joueur cible (`player`)                             |
| `elo_penalty`      | string   | Pénalité ELO éventuelle                                                     |
| `elo_after`        | string   | ELO du joueur après la partie                                               |
| `arena_win`        | string   | Variation de score arena                                                    |
| `arena_after`      | string   | Score arena après la partie                                                 |

### Notes importantes

- Tous les champs sont des **strings**, y compris les valeurs numériques (timestamps, scores, ELO).
- Les champs multi-valeurs (`players`, `player_names`, `scores`, `ranks`) utilisent la virgule comme séparateur. L'ordre est cohérent entre ces champs.
- Le `table_id` est la clé pour accéder au replay et aux détails de la partie (cartes jouées, etc.).
- Les timestamps `start` et `end` sont en secondes (Unix epoch).

## Headers requis

Les requêtes vers l'API BGA nécessitent les headers suivants :

| Header              | Valeur / Exemple                  | Description                                                        |
|----------------------|-----------------------------------|--------------------------------------------------------------------|
| `X-Request-Token`   | `6P16StDW2t5B0zH`                | Token anti-CSRF. Généré par BGA et présent dans la page HTML.      |
| `X-Requested-With`  | `XMLHttpRequest`                  | Indique une requête AJAX (obligatoire, sinon BGA peut rejeter).    |
| `Referer`           | `https://boardgamearena.com/gamestats?player=...` | Page d'origine. Doit pointer vers une page BGA cohérente. |
| `Cookie`            | *(cookies de session)*            | Transmis automatiquement par le navigateur / l'extension Chrome.   |

Headers standards du navigateur (envoyés automatiquement, pas besoin de les forcer) :
- `User-Agent` — identifiant du navigateur
- `sec-ch-ua`, `sec-ch-ua-platform`, `sec-ch-ua-mobile` — Client Hints (Chrome)

### Récupération du X-Request-Token

Le `X-Request-Token` est un token CSRF que BGA injecte dans chaque page via une variable JavaScript globale :

```javascript
bgaConfig.requestToken  // ex: "6P16StDW2t5B0zH"
```

`bgaConfig` est un objet global disponible sur toutes les pages BGA une fois connecté. L'extension Chrome peut y accéder en injectant un content script dans la page BGA :

```javascript
// Content script injecté dans boardgamearena.com
const token = window.bgaConfig.requestToken;
```

> **Note :** Les content scripts s'exécutent dans un monde isolé et n'ont pas accès aux variables JS de la page. Il faut soit :
> - Injecter un script dans le contexte de la page (`document.createElement('script')`) qui lit `bgaConfig.requestToken` et le communique via un `CustomEvent` ou `window.postMessage`
> - Utiliser `chrome.scripting.executeScript` avec `world: 'MAIN'` (Manifest V3)

## Pagination

- La première page est implicite (pas besoin du param `page`).
- Pour les pages suivantes, ajouter `page=2`, `page=3`, etc.
- Mettre `updateStats=1` uniquement sur le premier appel, `updateStats=0` ensuite.
- **TODO** : déterminer comment détecter la dernière page (tableau vide ? champ de metadata ?).

## Endpoint : logs d'une partie (replay)

```
GET https://boardgamearena.com/archive/archive/logs.html
```

Permet de récupérer l'intégralité des événements d'une partie à partir de son `table_id`.

### Query params

| Paramètre          | Exemple              | Description                                                        |
|---------------------|----------------------|--------------------------------------------------------------------|
| `table`            | `829783480`          | `table_id` de la partie (récupéré via l'endpoint `getGames`)       |
| `translated`       | `true`               | Retourne les messages de log traduits                               |
| `dojo.preventCache`| `1775032673941`      | Timestamp anti-cache                                                |

### Exemple de requête

```
https://boardgamearena.com/archive/archive/logs.html?table=829783480&translated=true&dojo.preventCache=1775032673941
```

### Headers requis

Mêmes headers que l'endpoint `getGames` (voir section Headers requis), avec :
- `Referer: https://boardgamearena.com/gamereview?table={table_id}`
- Les cookies de session sont **indispensables** pour cet endpoint (erreur 806 sans).

### Structure de la réponse

```
response.status   → 1 (succès)
response.data.logs[]  → tableau d'entrées de log (paquets réseau)
```

Chaque entrée de `logs[]` est un paquet réseau :

```json
{
    "channel": "/table/t829783480",
    "table_id": 829783480,
    "packet_id": "1",
    "packet_type": "resend",
    "move_id": "1",
    "time": "1774985974",
    "data": [ ... ]
}
```

| Champ         | Type     | Description                                                              |
|---------------|----------|--------------------------------------------------------------------------|
| `channel`     | string   | Canal de diffusion (`/table/t{id}` = public, `/player/p{id}` = privé)   |
| `table_id`    | int      | ID de la table                                                           |
| `packet_id`   | string   | Numéro séquentiel du paquet                                              |
| `packet_type` | string   | Type de paquet (observé : `"resend"`)                                    |
| `move_id`     | string   | Numéro du coup/action                                                    |
| `time`        | string   | Timestamp Unix de l'événement                                            |
| `data`        | array    | Liste d'événements dans ce paquet                                        |

### Structure d'un événement (`data[]`)

Chaque événement dans `data[]` a la structure :

```json
{
    "uid": "69cc22f63a4c6",
    "type": "playCard",
    "log": "${player_name} plays ${card_name} for ${mana_cost} and places it in ${displayLocation}",
    "args": { ... }
}
```

| Champ  | Type          | Description                                                              |
|--------|---------------|--------------------------------------------------------------------------|
| `uid`  | string        | Identifiant unique de l'événement                                        |
| `type` | string        | Type d'événement (voir liste ci-dessous)                                 |
| `log`  | string        | Template de message de log (avec placeholders `${...}`)                  |
| `args` | object/array  | Données associées à l'événement (variables du template + données métier) |

### Types d'événements observés

#### Événements de setup

| Type                              | Description                                                    |
|-----------------------------------|----------------------------------------------------------------|
| `gameStateChange`                 | Changement d'état de la machine d'état du jeu                  |
| `gameStateMultipleActiveUpdate`   | Mise à jour des joueurs actifs simultanés                      |
| `vsScreen`                        | Écran VS (factions en jeu)                                     |
| `setupPlayer`                     | Setup d'un joueur : faction, héros, meeples                    |
| `updateInformations`              | Mise à jour des biomes, mouvements, expéditions bloquées       |
| `updateInitialPrecoDeckSelection` | Sélection initiale des decks préco                             |
| `updateFirstDayManaSelection`     | Sélection du mana au premier jour                              |

#### Événements de jeu

| Type             | Description                                                          | Exemple de `log`                                                       |
|------------------|----------------------------------------------------------------------|------------------------------------------------------------------------|
| `newPhase`       | Nouvelle phase de jeu                                                | `Day n°${day}: afternoon`                                              |
| `playCard`       | Un joueur joue une carte                                             | `${player_name} plays ${card_name} for ${mana_cost}...`               |
| `drawCards`      | Un joueur pioche (vue publique, nombre seulement)                    | `${player_name} draws ${n} card(s) from its deck`                     |
| `pDrawCards`     | Un joueur pioche (vue privée, détail des cartes)                     | `You draw ${card_names} from your deck`                                |
| `discardCards`   | Un joueur défausse/place en mana (vue publique)                      | `${player_name} chooses ${n} card(s) as mana`                         |
| `pDiscardCards`  | Un joueur défausse/place en mana (vue privée, détail)                | `You place ${card_names} as mana`                                      |
| `newUndoableStep`| Point de sauvegarde pour le undo                                     | `Undo here`                                                            |

#### Événements système

| Type                    | Description                                   |
|-------------------------|-----------------------------------------------|
| `updateReflexionTime`   | Mise à jour du temps de réflexion d'un joueur |

### Extraction des informations de la partie

#### Héros joués

Les héros effectivement joués dans la partie sont dans les événements **`setupPlayer`** (canal public `/table/t{table_id}`). Il y a un événement par joueur.

```
setupPlayer.args.card.properties.name     → nom du héros (ex: "Della & Bolt")
setupPlayer.args.card.properties.faction  → faction du héros (ex: "AX")
setupPlayer.args.player_id               → ID BGA du joueur
setupPlayer.args.player_name             → pseudo BGA du joueur
```

Exemple :

```json
{
    "type": "setupPlayer",
    "args": {
        "player_name": "Yutsa",
        "player_id": "91926087",
        "faction_name": "Yzmir",
        "faction": "YZ",
        "card": {
            "id": 81,
            "properties": {
                "name": "Akesha & Taru",
                "faction": "YZ",
                "uid": "ALT_CORE_B_YZ_01_C",
                "type": "hero",
                "typeline": "Yzmir Hero"
            }
        }
    }
}
```

**Différencier le joueur courant de l'adversaire :** Le `player_id` dans `setupPlayer` est à comparer avec l'ID du joueur connecté (celui qui lance l'import depuis l'extension). L'extension connaît l'ID du joueur courant ; l'autre `setupPlayer` est l'adversaire.

#### Nom et ID du deck joué

Les informations du deck importé depuis l'API Altered sont dans l'événement **`updateInitialPrecoDeckSelection`** (canal privé `/player/p{player_id}`), dans l'objet `args.args._private.API`.

```
updateInitialPrecoDeckSelection.args.args._private.API.deckName  → nom du deck (ex: "Akesha Burst 4")
updateInitialPrecoDeckSelection.args.args._private.API.id        → ID du deck API Altered (ex: "01KKZV8Q0J0B49DS6JJ3QSCG4F")
updateInitialPrecoDeckSelection.args.args._private.API.faction   → faction du deck (ex: "YZ")
updateInitialPrecoDeckSelection.args.args._private.API.hero      → UID du héros (ex: "ALT_CORE_B_YZ_01_C")
updateInitialPrecoDeckSelection.args.args._private.API.cardCount → nombre de cartes dans le deck (ex: 39)
```

Exemple :

```json
{
    "type": "updateInitialPrecoDeckSelection",
    "channel": "/player/p91926087",
    "args": {
        "args": {
            "_private": {
                "API": {
                    "deckName": "Akesha Burst 4",
                    "id": "01KKZV8Q0J0B49DS6JJ3QSCG4F",
                    "faction": "YZ",
                    "legality": true,
                    "hero": "ALT_CORE_B_YZ_01_C",
                    "cardCount": 39,
                    "cards": { ... }
                }
            }
        }
    }
}
```

Il y a un événement `updateInitialPrecoDeckSelection` par joueur, chacun sur son canal `/player/p{player_id}`. En mode replay, **les données des deux joueurs sont accessibles** dans le JSON, quel que soit le joueur connecté.

Pour différencier le joueur courant de l'adversaire, comparer le `player_id` extrait du canal (`/player/p{player_id}`) avec l'ID du joueur qui lance l'import depuis l'extension.

#### Résumé : quelles données sont disponibles pour chaque joueur

| Information           | Source                                                   | Disponible pour |
|-----------------------|----------------------------------------------------------|-----------------|
| Nom du héros          | `setupPlayer` → `card.properties.name`                   | Les deux joueurs |
| Faction du héros      | `setupPlayer` → `card.properties.faction`                 | Les deux joueurs |
| Nom du deck           | `updateInitialPrecoDeckSelection` → `API.deckName`        | Les deux joueurs |
| ID du deck (API)      | `updateInitialPrecoDeckSelection` → `API.id`              | Les deux joueurs |

### Autres données dans les `args`

#### `playCard` — détail d'une carte jouée

```json
{
    "card": {
        "id": 42,
        "properties": {
            "uid": "ALT_CORE_B_AX_01_C",
            "name": "Sierra & Oddball",
            "faction": "AX",
            "type": "hero",
            "typeline": "Axiom Hero",
            "effectDesc": "...",
            "artist": "...",
            "extension": "SDU"
        }
    },
    "mana_cost": "2",
    "location": "expedition",
    "player_name": "Yutsa",
    "player_id": "91926087"
}
```

#### `pDrawCards` — cartes piochées (vue privée du joueur connecté)

```json
{
    "cards": [ { "id": 5, "properties": { "name": "...", "uid": "...", ... } } ],
    "player_id": "91926087",
    "card_names": "Card A, Card B"
}
```

#### Résultat de la partie

Le résultat est dans un tableau `result` présent dans les `args` d'un événement de type `gameStateChange` en fin de partie. Ce tableau contient un objet par joueur.

```
result[].player  → ID BGA du joueur
result[].name    → pseudo du joueur
result[].score   → "1" = victoire, "0" = défaite
result[].rank    → classement (1 = premier, 2 = second)
result[].concede → "1" si le joueur a concédé
result[].tie     → true si égalité
```

Exemple :

```json
{
    "result": [
        {
            "id": "91926087",
            "player": "91926087",
            "name": "Yutsa",
            "score": "1",
            "score_aux": "0",
            "zombie": "0",
            "concede": "0",
            "tie": false,
            "rank": 1
        },
        {
            "id": "95656346",
            "player": "95656346",
            "name": "Gibari13",
            "score": "0",
            "score_aux": "0",
            "zombie": "0",
            "concede": "0",
            "tie": false,
            "rank": 2
        }
    ]
}
```

## Rate limiting

BGA ne documente pas officiellement ses limites de requêtes. Le projet open source [bga-tournament-invitation](https://github.com/DavidEGx/bga-tournament-invitation) utilise un délai empirique de **1500ms entre chaque requête** comme mesure de précaution. Il n'y a pas de gestion d'erreur 429 (Too Many Requests) dans ce projet, ce qui suggère que ce délai suffit pour éviter le blocage.

**Recommandation** : espacer les requêtes d'au moins 1,5 seconde.

## Pagination

Lorsqu'une page demandée n'existe pas (au-delà des données disponibles), l'API retourne une réponse vide :

```json
{"status": 1, "data": {"tables": [], "stats": []}}
```

**Stratégie d'import** : l'extension permettra de préciser une date limite. On pagine en incrémentant `page` jusqu'à ce que :
- `data.tables` soit un tableau vide, ou
- toutes les parties restantes aient un `start` antérieur à la date limite choisie par l'utilisateur
