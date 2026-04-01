# INT-1 — Connecter l'extension au backend

**Phase :** 3 (intégration)
**Dépendances :** EXT-4, BACK-4
**Composant :** Extension Chrome + Backend Clojure

## Description

Brancher l'extension sur le backend local et valider que le flux fonctionne de bout en bout.

## Tâches

- [ ] Configurer l'URL du backend dans l'extension (hardcodé pour le MVP : `http://localhost:3000`)
- [ ] Ajouter l'URL du backend aux `host_permissions` du manifest
- [ ] Tester le flux complet : extension → POST replay → backend parse → stockage BDD
- [ ] Gérer les erreurs réseau côté extension (backend down, timeout)

## Critères d'acceptation

- L'extension envoie les replays au backend local sans erreur CORS
- Les erreurs réseau (backend inaccessible, timeout) sont affichées clairement dans le popup
- Le flux complet fonctionne : un replay envoyé depuis l'extension se retrouve en BDD
