# Spécification — Extension Chrome BGA

## Objectif

Récupérer l'historique de parties Altered de l'utilisateur depuis Board Game Arena et l'envoyer au backend AlteredHistory.

## Principe de fonctionnement

L'extension s'exécute dans le navigateur de l'utilisateur connecté à BGA. Elle bénéficie de deux avantages clés :
- **Accès aux cookies de session BGA** — l'utilisateur n'a pas besoin de fournir ses identifiants
- **Pas de restriction CORS** — les extensions Chrome avec `host_permissions` peuvent appeler n'importe quel domaine

## Permissions requises

```json
{
  "manifest_version": 3,
  "host_permissions": [
    "https://boardgamearena.com/*",
    "https://*.boardgamearena.com/*"
  ],
  "permissions": [
    "cookies"
  ]
}
```

- `host_permissions` sur BGA : permet les appels HTTP vers l'API BGA et l'accès aux cookies du domaine
- `cookies` : accès explicite aux cookies de session BGA
- L'URL du backend sera ajoutée aux `host_permissions` une fois déterminée

## Flow utilisateur

1. L'utilisateur se connecte sur boardgamearena.com dans son navigateur
2. Il ouvre l'extension (clic sur l'icône ou popup)
3. L'extension vérifie que l'utilisateur est connecté à BGA (présence du cookie de session)
4. L'utilisateur déclenche la synchronisation
5. L'extension appelle l'API BGA pour récupérer l'historique de parties Altered
6. Les données sont envoyées au backend AlteredHistory via POST
7. L'extension affiche un résumé (nombre de parties synchronisées, erreurs éventuelles)

## Composants

### Popup (UI)

Interface minimale :
- Indicateur de connexion BGA (connecté / non connecté)
- Bouton "Synchroniser"
- Retour visuel : progression, résultat, erreurs

### Service Worker (background)

Responsable de :
- Vérifier la session BGA (cookie valide)
- Appeler les endpoints BGA pour récupérer l'historique
- Formater les données
- Envoyer les données au backend via POST

### Communication popup ↔ service worker

Via `chrome.runtime.sendMessage` / `chrome.runtime.onMessage` (pattern standard Manifest V3).

## API BGA

Voir [bga_api.md](bga_api.md) pour la documentation complète de l'API BGA (endpoints, paramètres, structure de réponse).

## Données collectées

Pour chaque partie, l'extension transmet au minimum :
- Identifiant de la partie (BGA)
- Date de la partie
- Résultat (victoire/défaite)
- Adversaire(s)
- Toute métadonnée spécifique à Altered disponible (faction jouée, etc.)

Le format exact sera affiné après investigation de l'API BGA.

## Envoi au backend

```
POST /api/sync
Content-Type: application/json

{
  "user_id": "bga_user_id",
  "games": [
    {
      "bga_game_id": "...",
      "date": "...",
      "result": "...",
      "opponent": "...",
      ...
    }
  ]
}
```

## Gestion des erreurs

- **Utilisateur non connecté à BGA** : message clair invitant à se connecter sur BGA
- **Session expirée** : détection et notification
- **Erreur réseau BGA** : retry avec backoff, message d'erreur
- **Erreur backend** : message d'erreur avec possibilité de réessayer
- **Rate limiting BGA** : respecter les délais, ne pas spammer les requêtes

## Contraintes

- Manifest V3 (standard actuel des extensions Chrome)
- L'extension ne stocke aucune donnée localement au-delà de la session en cours
- Aucun credential BGA n'est stocké ni transmis — seuls les cookies de session du navigateur sont utilisés
- Code en JavaScript (contrainte de la plateforme Chrome Extensions)


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
