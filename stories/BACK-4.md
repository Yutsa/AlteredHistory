# BACK-4 — Endpoint POST /api/replays

**Phase :** 2 (séquentielle)
**Dépendances :** BACK-3
**Composant :** Backend Clojure

## Description

Implémenter l'endpoint HTTP qui reçoit les replays bruts depuis l'extension, les parse et les stocke en BDD.

## Contrat d'API

### Requête

```
POST /api/replays
Content-Type: application/json

<JSON de replay BGA brut, tel que retourné par logs.html>
```

### Réponses

| Status | Condition | Body |
|--------|-----------|------|
| `201 Created` | Partie insérée avec succès | `{"table_id": "...", "status": "created"}` |
| `200 OK` | Partie déjà existante (idempotence) | `{"table_id": "...", "status": "already_exists"}` |
| `400 Bad Request` | Erreur de parsing du replay | `{"error": "...", "details": "..."}` |

## Tâches

- [ ] Implémenter `POST /api/replays` :
  1. Recevoir le JSON de replay brut
  2. Appeler le parser (BACK-3)
  3. Vérifier si le `table_id` existe déjà (idempotence)
  4. Insérer en BDD si nouveau
  5. Retourner le status approprié
- [ ] Gérer les erreurs de parsing → `400 Bad Request` avec message explicatif

## Critères d'acceptation

- Un replay valide envoyé en POST est parsé et stocké en BDD → `201`
- Le même replay envoyé une seconde fois retourne `200` sans doublon
- Un replay invalide/incomplet retourne `400` avec un message d'erreur utile
- Le endpoint accepte le JSON tel quel (pas de transformation côté extension)
